package lk.swiftgolanka.service;

import lk.swiftgolanka.dto.ComplaintDTO;
import lk.swiftgolanka.dto.FeedbackDTO;
import lk.swiftgolanka.dto.ReportRequestDTO;
import lk.swiftgolanka.entity.*;
import lk.swiftgolanka.enums.ComplaintStatus;
import lk.swiftgolanka.enums.ReportType;
import lk.swiftgolanka.exception.InvalidOperationException;
import lk.swiftgolanka.exception.ResourceNotFoundException;
import lk.swiftgolanka.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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
    private NotificationService notificationService;

    // --- Feedback Methods ---

    @Transactional
    public Feedback submitFeedback(FeedbackDTO dto, User passenger) {
        Trip trip = tripRepository.findById(dto.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + dto.getTripId()));

        if (!trip.getPassenger().getId().equals(passenger.getId())) {
            throw new InvalidOperationException("You can only submit feedback for trips you completed.");
        }

        Feedback feedback = Feedback.builder()
                .trip(trip)
                .passenger(passenger)
                .driver(trip.getDriver())
                .rating(dto.getRating())
                .comment(dto.getComment())
                .isDeleted(false)
                .build();

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

    // --- Report Methods ---

    @Transactional
    public Report generateReport(ReportRequestDTO dto, User staffUser) {
        LocalDate startDate = dto.getStartDate() != null ? dto.getStartDate() : LocalDate.now().minusDays(30);
        LocalDate endDate = dto.getEndDate() != null ? dto.getEndDate() : LocalDate.now();

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

    public List<Report> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }
}
