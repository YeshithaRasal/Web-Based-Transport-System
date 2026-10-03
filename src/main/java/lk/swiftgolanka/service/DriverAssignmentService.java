package lk.swiftgolanka.service;

import lk.swiftgolanka.entity.DriverAssignment;
import lk.swiftgolanka.entity.DriverProfile;
import lk.swiftgolanka.entity.RideBooking;
import lk.swiftgolanka.entity.User;
import lk.swiftgolanka.enums.AssignmentStatus;
import lk.swiftgolanka.enums.AvailabilityStatus;
import lk.swiftgolanka.enums.BookingStatus;
import lk.swiftgolanka.enums.VehicleType;
import lk.swiftgolanka.enums.VerificationStatus;
import lk.swiftgolanka.exception.InvalidOperationException;
import lk.swiftgolanka.exception.ResourceNotFoundException;
import lk.swiftgolanka.repository.DriverAssignmentRepository;
import lk.swiftgolanka.repository.DriverProfileRepository;
import lk.swiftgolanka.repository.RideBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DriverAssignmentService {

    @Autowired
    private DriverAssignmentRepository assignmentRepository;

    @Autowired
    private DriverProfileRepository driverProfileRepository;

    @Autowired
    private RideBookingRepository bookingRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TripService tripService;

    @Transactional
    public boolean autoAssignDriverForBooking(RideBooking booking) {
        // Broadcast to ALL verified available drivers
        List<DriverProfile> availableDrivers = driverProfileRepository.findAvailableDrivers();

        if (availableDrivers.isEmpty()) {
            booking.setBookingStatus(BookingStatus.SEARCHING_DRIVER);
            bookingRepository.save(booking);
            return false;
        }

        // Broadcast to ALL available drivers so every online driver sees the request!
        boolean assignedAny = false;
        for (DriverProfile driver : availableDrivers) {
            if (createAssignment(booking, driver)) {
                assignedAny = true;
            }
        }
        return assignedAny;
    }

    @Transactional
    public boolean createAssignment(RideBooking booking, DriverProfile driver) {
        if (assignmentRepository.findByBookingIdAndDriverId(booking.getId(), driver.getId()).isPresent()) {
            return false;
        }

        DriverAssignment assignment = DriverAssignment.builder()
                .booking(booking)
                .driver(driver)
                .assignmentStatus(AssignmentStatus.PENDING)
                .build();

        assignmentRepository.save(assignment);

        booking.setBookingStatus(BookingStatus.SEARCHING_DRIVER);
        bookingRepository.save(booking);

        notificationService.sendNotification(driver.getUser(), "New Ride Request!",
                "Pickup at: " + booking.getPickupLocation() + " (Est. Fare: Rs. " + booking.getEstimatedFare() + ")");
        return true;
    }

    @Transactional
    public void manualAssignDriver(Long bookingId, Long driverProfileId) {
        RideBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        DriverProfile driver = driverProfileRepository.findById(driverProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with ID: " + driverProfileId));

        createAssignment(booking, driver);
    }

    @Transactional
    public void driverAcceptAssignment(Long assignmentId, User driverUser) {
        DriverAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment request not found: " + assignmentId));

        if (!assignment.getDriver().getUser().getId().equals(driverUser.getId())) {
            throw new InvalidOperationException("This assignment does not belong to you.");
        }

        RideBooking booking = assignment.getBooking();
        if (booking.getBookingStatus() != BookingStatus.REQUESTED && booking.getBookingStatus() != BookingStatus.SEARCHING_DRIVER) {
            assignment.setAssignmentStatus(AssignmentStatus.CANCELLED);
            assignmentRepository.save(assignment);
            throw new InvalidOperationException("This ride request has already been accepted by another driver.");
        }

        assignment.setAssignmentStatus(AssignmentStatus.ACCEPTED);
        assignment.setRespondedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        // Cancel / dismiss pending assignments for all other drivers for this booking
        List<DriverAssignment> allAssignments = assignmentRepository.findByBookingId(booking.getId());
        for (DriverAssignment other : allAssignments) {
            if (!other.getId().equals(assignment.getId()) && other.getAssignmentStatus() == AssignmentStatus.PENDING) {
                other.setAssignmentStatus(AssignmentStatus.CANCELLED);
                other.setRespondedAt(LocalDateTime.now());
                assignmentRepository.save(other);
            }
        }

        // Update Driver status to BUSY
        DriverProfile driverProfile = assignment.getDriver();
        driverProfile.setAvailabilityStatus(AvailabilityStatus.BUSY);
        driverProfileRepository.save(driverProfile);

        // Update Booking status to DRIVER_ASSIGNED
        booking.setBookingStatus(BookingStatus.DRIVER_ASSIGNED);
        bookingRepository.save(booking);

        // Automatically create Trip record!
        tripService.createTripFromAssignment(assignment);

        notificationService.sendNotification(booking.getPassenger(), "Driver Accepted Your Ride!",
                driverProfile.getUser().getFullName() + " (" + driverProfile.getVehicleNumber() + " - " + driverProfile.getVehicleModel() + ") is on the way!");
    }

    @Transactional
    public void driverRejectAssignment(Long assignmentId, User driverUser) {
        DriverAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment request not found: " + assignmentId));

        if (!assignment.getDriver().getUser().getId().equals(driverUser.getId())) {
            throw new InvalidOperationException("This assignment does not belong to you.");
        }

        assignment.setAssignmentStatus(AssignmentStatus.REJECTED);
        assignment.setRespondedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        // Notify passenger and set booking back to REQUESTED so customer can select another driver
        RideBooking booking = assignment.getBooking();
        if (booking.getBookingStatus() == BookingStatus.SEARCHING_DRIVER || booking.getBookingStatus() == BookingStatus.REQUESTED) {
            booking.setBookingStatus(BookingStatus.REQUESTED);
            bookingRepository.save(booking);
        }

        notificationService.sendNotification(booking.getPassenger(), "Driver Declined Ride Request",
                "Driver " + assignment.getDriver().getUser().getFullName() + " declined your request. Please select another available driver.");
    }

    public List<DriverProfile> getAvailableDriversForCustomer(VehicleType preferredType) {
        List<DriverProfile> matched = driverProfileRepository.findAvailableDriversByVehicleType(preferredType);
        if (!matched.isEmpty()) {
            return matched;
        }
        return driverProfileRepository.findAvailableDrivers();
    }

    public List<DriverAssignment> getAssignmentsForBooking(Long bookingId) {
        return assignmentRepository.findByBookingId(bookingId);
    }

    public Optional<DriverAssignment> getLatestAssignmentForBooking(Long bookingId) {
        List<DriverAssignment> list = assignmentRepository.findByBookingId(bookingId);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(list.size() - 1));
    }

    @Transactional
    public DriverAssignment selectDriverForBooking(Long bookingId, Long driverProfileId, User passenger) {
        RideBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!booking.getPassenger().getId().equals(passenger.getId())) {
            throw new InvalidOperationException("You can only select drivers for your own bookings.");
        }

        if (booking.getBookingStatus() != BookingStatus.REQUESTED && booking.getBookingStatus() != BookingStatus.SEARCHING_DRIVER) {
            throw new InvalidOperationException("Cannot select a driver for a booking that is " + booking.getBookingStatus());
        }

        DriverProfile driver = driverProfileRepository.findById(driverProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + driverProfileId));

        if (driver.getAvailabilityStatus() != AvailabilityStatus.AVAILABLE || driver.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new InvalidOperationException("Selected driver is currently unavailable or not verified. Please choose another driver.");
        }

        // Cancel previous pending assignments for this booking
        cancelPendingAssignmentsForBooking(bookingId);

        DriverAssignment assignment = DriverAssignment.builder()
                .booking(booking)
                .driver(driver)
                .assignmentStatus(AssignmentStatus.PENDING)
                .build();

        DriverAssignment saved = assignmentRepository.save(assignment);

        booking.setBookingStatus(BookingStatus.SEARCHING_DRIVER);
        bookingRepository.save(booking);

        notificationService.sendNotification(driver.getUser(), "New Ride Request!",
                "Passenger " + passenger.getFullName() + " selected you for a trip from " + booking.getPickupLocation() + " to " + booking.getDestinationLocation() + " (Est. Fare: Rs. " + booking.getEstimatedFare() + ")");
        return saved;
    }

    @Transactional
    public void cancelPendingAssignmentsForBooking(Long bookingId) {
        List<DriverAssignment> assignments = assignmentRepository.findByBookingId(bookingId);
        for (DriverAssignment da : assignments) {
            if (da.getAssignmentStatus() == AssignmentStatus.PENDING) {
                da.setAssignmentStatus(AssignmentStatus.CANCELLED);
                da.setRespondedAt(LocalDateTime.now());
                assignmentRepository.save(da);
            }
        }
    }

    @Transactional
    public void toggleDriverAvailability(Long driverUserId, AvailabilityStatus newStatus) {
        DriverProfile profile = driverProfileRepository.findByUserId(driverUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));

        if (profile.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new InvalidOperationException("You cannot change availability until your driver profile is verified by an Admin.");
        }

        profile.setAvailabilityStatus(newStatus);
        driverProfileRepository.save(profile);

        // When a driver becomes AVAILABLE, check for any open ride requests that need drivers
        if (newStatus == AvailabilityStatus.AVAILABLE) {
            List<RideBooking> pendingBookings = bookingRepository.findByBookingStatusIn(
                    List.of(BookingStatus.REQUESTED, BookingStatus.SEARCHING_DRIVER)
            );
            for (RideBooking bk : pendingBookings) {
                createAssignment(bk, profile);
            }
        }
    }

    public List<DriverAssignment> getPendingAssignmentsForDriver(Long driverUserId) {
        DriverProfile profile = driverProfileRepository.findByUserId(driverUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));
        return assignmentRepository.findPendingAssignmentsForDriver(profile.getId());
    }

    public List<DriverAssignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    @Transactional
    public void adminCancelAssignment(Long assignmentId) {
        DriverAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));
        assignment.setAssignmentStatus(AssignmentStatus.CANCELLED);
        assignmentRepository.save(assignment);
    }
}
