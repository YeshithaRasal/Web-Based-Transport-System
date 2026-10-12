package lk.swiftgolanka.service;

import lk.swiftgolanka.dto.ComplaintDTO;
import lk.swiftgolanka.dto.FeedbackDTO;
import lk.swiftgolanka.dto.ReportRequestDTO;
import lk.swiftgolanka.entity.*;
import lk.swiftgolanka.enums.BookingStatus;
import lk.swiftgolanka.enums.ComplaintStatus;
import lk.swiftgolanka.enums.PaymentStatus;
import lk.swiftgolanka.enums.ReportType;
import lk.swiftgolanka.enums.TripStatus;
import lk.swiftgolanka.exception.InvalidOperationException;
import lk.swiftgolanka.exception.ResourceNotFoundException;
import lk.swiftgolanka.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lk.swiftgolanka.dto.GeneratedReportDTO;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportFeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private DriverProfileRepository driverProfileRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RideBookingRepository bookingRepository;

    @Autowired
    private NotificationService notificationService;

    // --- Feedback Methods ---

    @Transactional
    public Feedback submitFeedback(FeedbackDTO dto, User passenger) {
        Trip trip = tripRepository.findById(dto.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + dto.getTripId()));

        if (!trip.getPassenger().getId().equals(passenger.getId())) {
            throw new InvalidOperationException("You can only submit feedback for trips you completed.");
        }

        Optional<Feedback> existingOpt = feedbackRepository.findByTripIdAndIsDeletedFalse(trip.getId());
        Feedback feedback;
        if (existingOpt.isPresent()) {
            feedback = existingOpt.get();
            feedback.setRating(dto.getRating());
            feedback.setComment(dto.getComment());
        } else {
            feedback = Feedback.builder()
                    .trip(trip)
                    .passenger(passenger)
                    .driver(trip.getDriver())
                    .rating(dto.getRating())
                    .comment(dto.getComment())
                    .isDeleted(false)
                    .build();
        }

        Feedback saved = feedbackRepository.save(feedback);

        // Recalculate driver average rating
        DriverProfile driverProfile = trip.getDriver();
        Double avg = feedbackRepository.calculateAverageRatingForDriver(driverProfile.getId());
        if (avg != null) {
            driverProfile.setRatingAvg(Math.round(avg * 10.0) / 10.0);
            driverProfileRepository.save(driverProfile);
        }

        notificationService.sendNotification(driverProfile.getUser(), "New Rating Received!", "A passenger rated your trip " + dto.getRating() + " stars.");
        return saved;
    }

    public List<Feedback> getPassengerFeedback(Long passengerId) {
        return feedbackRepository.findByPassengerIdAndIsDeletedFalseOrderByCreatedAtDesc(passengerId);
    }

    public List<Trip> getUnreviewedCompletedTripsForPassenger(Long passengerId) {
        List<Trip> trips = tripRepository.findByPassengerIdOrderByStartTimeDesc(passengerId);
        List<Feedback> feedbackList = feedbackRepository.findByPassengerIdAndIsDeletedFalseOrderByCreatedAtDesc(passengerId);
        Set<Long> reviewedTripIds = feedbackList.stream()
                .map(f -> f.getTrip().getId())
                .collect(Collectors.toSet());

        return trips.stream()
                .filter(t -> t.getTripStatus() == TripStatus.COMPLETED && !reviewedTripIds.contains(t.getId()))
                .collect(Collectors.toList());
    }

    public Set<Long> getReviewedTripIdsForPassenger(Long passengerId) {
        return feedbackRepository.findByPassengerIdAndIsDeletedFalseOrderByCreatedAtDesc(passengerId).stream()
                .map(f -> f.getTrip().getId())
                .collect(Collectors.toSet());
    }

    public List<Feedback> getDriverFeedback(Long driverUserId) {
        DriverProfile profile = driverProfileRepository.findByUserId(driverUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));
        return feedbackRepository.findByDriverIdAndIsDeletedFalseOrderByCreatedAtDesc(profile.getId());
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findByIsDeletedFalseOrderByCreatedAtDesc();
    }

    @Transactional
    public void softDeleteFeedback(Long feedbackId) {
        Feedback f = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found: " + feedbackId));
        f.setDeleted(true);
        feedbackRepository.save(f);
    }

    // --- Complaint Methods ---

    @Transactional
    public Complaint submitComplaint(ComplaintDTO dto, User user) {
        Trip trip = null;
        if (dto.getTripId() != null) {
            trip = tripRepository.findById(dto.getTripId()).orElse(null);
        }

        Complaint complaint = Complaint.builder()
                .user(user)
                .trip(trip)
                .subject(dto.getSubject())
                .category(dto.getCategory())
                .description(dto.getDescription())
                .complaintStatus(ComplaintStatus.OPEN)
                .build();

        notificationService.sendNotification(user, "Complaint Submitted", "Ticket #" + complaint.getSubject() + " has been logged with Customer Support.");
        return complaintRepository.save(complaint);
    }

    public List<Complaint> getUserComplaints(Long userId) {
        return complaintRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void updateComplaintStatus(Long complaintId, ComplaintStatus newStatus, String resolutionNotes) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint ticket not found: " + complaintId));

        complaint.setComplaintStatus(newStatus);
        if (resolutionNotes != null && !resolutionNotes.isBlank()) {
            complaint.setResolutionNotes(resolutionNotes);
        }

        complaintRepository.save(complaint);
        notificationService.sendNotification(complaint.getUser(), "Complaint Status Updated", "Your complaint ticket '" + complaint.getSubject() + "' status changed to " + newStatus.name());
    }

    @Transactional
    public void resolveComplaint(Long complaintId, String resolutionNotes, User admin) {
        updateComplaintStatus(complaintId, ComplaintStatus.RESOLVED, resolutionNotes);
    }

    @Transactional
    public void deleteComplaint(Long complaintId, User user) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint ticket not found: " + complaintId));
        complaintRepository.delete(complaint);
    }

    // --- Report Methods ---

    @Transactional
    public Report generateReport(ReportRequestDTO dto, User staffUser) {
        LocalDate startDate = dto.getStartDate() != null ? dto.getStartDate() : LocalDate.of(2020, 1, 1);
        LocalDate endDate = dto.getEndDate() != null ? dto.getEndDate() : LocalDate.now().plusYears(1);

        if (startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        String title = dto.getTitle() != null && !dto.getTitle().isBlank() ?
                dto.getTitle() : dto.getReportType().getDisplayName() + " (" + startDate + " to " + endDate + ")";

        Report report = Report.builder()
                .reportType(dto.getReportType())
                .title(title)
                .startDate(startDate)
                .endDate(endDate)
                .generatedBy(staffUser)
                .build();

        return reportRepository.save(report);
    }

    @Transactional
    public GeneratedReportDTO generateReportData(ReportRequestDTO dto, User staffUser) {
        Report report = generateReport(dto, staffUser);

        LocalDate startDate = report.getStartDate();
        LocalDate endDate = report.getEndDate();
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<String> headers = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();
        Map<String, String> summaryStats = new LinkedHashMap<>();

        ReportType type = report.getReportType();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        switch (type) {
            case DRIVER_PERFORMANCE:
                headers = List.of("Driver ID", "Driver Name", "Licence No", "Vehicle No", "Vehicle Type", "Completed Trips", "Average Rating", "Total Income (Rs.)");
                List<DriverProfile> drivers = driverProfileRepository.findAll();
                double grandIncome = 0.0;
                long grandTrips = 0;
                for (DriverProfile d : drivers) {
                    Long tripCount = paymentRepository.countPaidTripsForDriver(d.getId());
                    Double income = paymentRepository.calculateTotalIncomeForDriver(d.getId());
                    long tCount = tripCount != null ? tripCount : 0L;
                    double incVal = income != null ? income : 0.0;
                    grandTrips += tCount;
                    grandIncome += incVal;
                    String dName = d.getUser() != null ? d.getUser().getFullName() : ("Driver #" + d.getId());
                    String vType = d.getVehicleType() != null ? d.getVehicleType().name() : "N/A";
                    String lic = d.getLicenceNumber() != null ? d.getLicenceNumber() : "N/A";
                    String veh = d.getVehicleNumber() != null ? d.getVehicleNumber() : "N/A";
                    rows.add(List.of(
                            "#" + d.getId(),
                            dName,
                            lic,
                            veh,
                            vType,
                            String.valueOf(tCount),
                            String.format("%.1f ★", d.getRatingAvg() != null ? d.getRatingAvg() : 5.0),
                            String.format("Rs. %.2f", incVal)
                    ));
                }
                summaryStats.put("Total Registered Drivers", String.valueOf(drivers.size()));
                summaryStats.put("Total Completed Paid Trips", String.valueOf(grandTrips));
                summaryStats.put("Grand Driver Payout", String.format("Rs. %.2f", grandIncome));
                break;

            case FINANCIAL_SUMMARY:
            case FINANCIAL:
                headers = List.of("Payment ID", "Txn Ref", "Trip #", "Passenger", "Driver", "Method", "Card / Account", "Amount (Rs.)", "Status", "Date & Time");
                List<Payment> allPayments = paymentRepository.findAll();
                double totalRev = 0.0;
                int countPaid = 0;
                for (Payment p : allPayments) {
                    LocalDateTime pTime = p.getPaidAt();
                    if (pTime != null && (pTime.isBefore(startDateTime) || pTime.isAfter(endDateTime))) {
                        continue;
                    }
                    if (p.getPaymentStatus() == PaymentStatus.PAID) {
                        countPaid++;
                        totalRev += (p.getAmount() != null ? p.getAmount() : 0.0);
                    }
                    String cardDisplay = p.getMaskedCardNumber() != null ? p.getMaskedCardNumber() : (p.getCardHolderName() != null ? p.getCardHolderName() : "N/A");
                    String tripRef = p.getTrip() != null ? "#" + p.getTrip().getId() : "N/A";
                    String passName = p.getPassenger() != null ? p.getPassenger().getFullName() : "N/A";
                    String drvName = (p.getDriver() != null && p.getDriver().getUser() != null) ? p.getDriver().getUser().getFullName() :
                            (p.getTrip() != null && p.getTrip().getDriver() != null && p.getTrip().getDriver().getUser() != null ? p.getTrip().getDriver().getUser().getFullName() : "N/A");
                    String pMethod = p.getPaymentMethod() != null ? p.getPaymentMethod().name() : "CARD";
                    String pStatus = p.getPaymentStatus() != null ? p.getPaymentStatus().name() : "PAID";
                    String txnRef = p.getTransactionReference() != null ? p.getTransactionReference() : "N/A";
                    String timeStr = pTime != null ? pTime.format(dtf) : "N/A";
                    rows.add(List.of(
                            "#" + p.getId(),
                            txnRef,
                            tripRef,
                            passName,
                            drvName,
                            pMethod,
                            cardDisplay,
                            String.format("Rs. %.2f", p.getAmount() != null ? p.getAmount() : 0.0),
                            pStatus,
                            timeStr
                    ));
                }
                summaryStats.put("Total Transactions Evaluated", String.valueOf(rows.size()));
                summaryStats.put("Successful Paid Transactions", String.valueOf(countPaid));
                summaryStats.put("Total Revenue Collected", String.format("Rs. %.2f", totalRev));
                summaryStats.put("Platform Net Fee (15%)", String.format("Rs. %.2f", totalRev * 0.15));
                summaryStats.put("Driver Total Earnings (85%)", String.format("Rs. %.2f", totalRev * 0.85));
                break;

            case TRIP_STATISTICS:
            case RIDE_ACTIVITY:
            case OPERATIONAL_PERFORMANCE:
                headers = List.of("Trip ID", "Booking #", "Passenger", "Driver", "Pickup", "Destination", "Distance (km)", "Fare (Rs.)", "Status", "Start Time", "End Time");
                List<Trip> trips = tripRepository.findAll();
                double totalDistance = 0.0;
                long completed = 0;
                long totalInRange = 0;
                for (Trip t : trips) {
                    LocalDateTime tTime = t.getStartTime();
                    if (tTime != null && (tTime.isBefore(startDateTime) || tTime.isAfter(endDateTime))) {
                        continue;
                    }
                    totalInRange++;
                    if (t.getTripStatus() == TripStatus.COMPLETED) completed++;
                    if (t.getDistanceKm() != null) totalDistance += t.getDistanceKm();

                    String bRef = t.getBooking() != null ? "#" + t.getBooking().getId() : "N/A";
                    String pName = t.getPassenger() != null ? t.getPassenger().getFullName() : "N/A";
                    String dName = (t.getDriver() != null && t.getDriver().getUser() != null) ? t.getDriver().getUser().getFullName() : "N/A";
                    String pick = t.getPickupLocation() != null ? t.getPickupLocation() : "N/A";
                    String dest = t.getDestinationLocation() != null ? t.getDestinationLocation() : "N/A";
                    String distStr = t.getDistanceKm() != null ? String.format("%.1f km", t.getDistanceKm()) : "N/A";
                    String fareStr = t.getFinalFare() != null ? String.format("Rs. %.2f", t.getFinalFare()) : "N/A";
                    String stStr = t.getTripStatus() != null ? t.getTripStatus().name() : "N/A";
                    String startStr = t.getStartTime() != null ? t.getStartTime().format(dtf) : "N/A";
                    String endStr = t.getEndTime() != null ? t.getEndTime().format(dtf) : "N/A";

                    rows.add(List.of(
                            "#" + t.getId(),
                            bRef,
                            pName,
                            dName,
                            pick,
                            dest,
                            distStr,
                            fareStr,
                            stStr,
                            startStr,
                            endStr
                    ));
                }
                summaryStats.put("Total Trips in Range", String.valueOf(totalInRange));
                summaryStats.put("Completed Trips", String.valueOf(completed));
                summaryStats.put("Total Travelled Distance", String.format("%.1f km", totalDistance));
                break;

            case BOOKING_ANALYSIS:
                headers = List.of("Booking ID", "Passenger", "Pickup", "Destination", "Vehicle Type", "Est. Distance", "Est. Fare", "Status", "Created Time");
                List<RideBooking> bookings = bookingRepository.findAllRecentBookings();
                long reqCount = 0;
                long compCount = 0;
                for (RideBooking b : bookings) {
                    if (b.getCreatedAt() != null && (b.getCreatedAt().isBefore(startDateTime) || b.getCreatedAt().isAfter(endDateTime))) {
                        continue;
                    }
                    if (b.getBookingStatus() == BookingStatus.COMPLETED) compCount++;
                    reqCount++;
                    String passName = b.getPassenger() != null ? b.getPassenger().getFullName() : "N/A";
                    String pick = b.getPickupLocation() != null ? b.getPickupLocation() : "N/A";
                    String dest = b.getDestinationLocation() != null ? b.getDestinationLocation() : "N/A";
                    String vType = b.getVehicleType() != null ? b.getVehicleType().name() : "CAR";
                    String distStr = b.getEstimatedDistanceKm() != null ? String.format("%.1f km", b.getEstimatedDistanceKm()) : "N/A";
                    String fareStr = b.getEstimatedFare() != null ? String.format("Rs. %.2f", b.getEstimatedFare()) : "N/A";
                    String bStat = b.getBookingStatus() != null ? b.getBookingStatus().name() : "REQUESTED";
                    String createdStr = b.getCreatedAt() != null ? b.getCreatedAt().format(dtf) : "N/A";

                    rows.add(List.of(
                            "#" + b.getId(),
                            passName,
                            pick,
                            dest,
                            vType,
                            distStr,
                            fareStr,
                            bStat,
                            createdStr
                    ));
                }
                summaryStats.put("Total Bookings in Range", String.valueOf(reqCount));
                summaryStats.put("Completed Bookings", String.valueOf(compCount));
                break;

            case CUSTOMER_FEEDBACK:
            case CUSTOMER_SATISFACTION:
            default:
                headers = List.of("Feedback ID", "Trip ID", "Passenger", "Driver", "Rating", "Comments", "Submitted At");
                List<Feedback> feedbackList = feedbackRepository.findByIsDeletedFalseOrderByCreatedAtDesc();
                double ratingSum = 0.0;
                int fbCount = 0;
                for (Feedback f : feedbackList) {
                    if (f.getCreatedAt() != null && (f.getCreatedAt().isBefore(startDateTime) || f.getCreatedAt().isAfter(endDateTime))) {
                        continue;
                    }
                    fbCount++;
                    ratingSum += (f.getRating() != null ? f.getRating() : 5);
                    String tripRef = f.getTrip() != null ? "#" + f.getTrip().getId() : "N/A";
                    String passName = f.getPassenger() != null ? f.getPassenger().getFullName() : "Anonymous";
                    String drvName = (f.getDriver() != null && f.getDriver().getUser() != null) ? f.getDriver().getUser().getFullName() : "Driver";
                    String stars = (f.getRating() != null ? f.getRating() : 5) + " ★";
                    String comm = f.getComment() != null ? f.getComment() : "";
                    String submittedStr = f.getCreatedAt() != null ? f.getCreatedAt().format(dtf) : "N/A";

                    rows.add(List.of(
                            "#" + f.getId(),
                            tripRef,
                            passName,
                            drvName,
                            stars,
                            comm,
                            submittedStr
                    ));
                }
                summaryStats.put("Total Reviews in Range", String.valueOf(fbCount));
                summaryStats.put("Average Score", fbCount > 0 ? String.format("%.2f ★", ratingSum / fbCount) : "5.0 ★");
                break;
        }

        return new GeneratedReportDTO(report, headers, rows, summaryStats);
    }

    public GeneratedReportDTO getReportDataById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));
        ReportRequestDTO req = new ReportRequestDTO();
        req.setReportType(report.getReportType());
        req.setTitle(report.getTitle());
        req.setStartDate(report.getStartDate());
        req.setEndDate(report.getEndDate());
        return generateReportData(req, report.getGeneratedBy());
    }

    public List<Report> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }
}
