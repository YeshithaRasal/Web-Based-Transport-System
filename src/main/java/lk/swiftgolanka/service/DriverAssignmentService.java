package lk.swiftgolanka.service;

import lk.swiftgolanka.entity.DriverAssignment;
import lk.swiftgolanka.entity.DriverProfile;
import lk.swiftgolanka.entity.RideBooking;
import lk.swiftgolanka.entity.User;
import lk.swiftgolanka.enums.AssignmentStatus;
import lk.swiftgolanka.enums.AvailabilityStatus;
import lk.swiftgolanka.enums.BookingStatus;
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
        List<DriverProfile> availableDrivers = driverProfileRepository
                .findAvailableDriversByVehicleType(booking.getVehicleType());

        if (availableDrivers.isEmpty()) {
            // Fallback: search any available driver if matching vehicle type isn't online
            availableDrivers = driverProfileRepository.findAvailableDrivers();
        }

        if (availableDrivers.isEmpty()) {
            booking.setBookingStatus(BookingStatus.SEARCHING_DRIVER);
            bookingRepository.save(booking);
            return false;
        }

        // Pick first available driver
        DriverProfile selectedDriver = availableDrivers.get(0);
        return createAssignment(booking, selectedDriver);
    }

    @Transactional
    public boolean createAssignment(RideBooking booking, DriverProfile driver) {
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

        assignment.setAssignmentStatus(AssignmentStatus.ACCEPTED);
        assignment.setRespondedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        // Update Driver status to BUSY
        DriverProfile driverProfile = assignment.getDriver();
        driverProfile.setAvailabilityStatus(AvailabilityStatus.BUSY);
        driverProfileRepository.save(driverProfile);

        // Update Booking status to DRIVER_ASSIGNED
        RideBooking booking = assignment.getBooking();
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

        assignment.setAssignmentStatus(AssignmentStatus.REJECTED);
        assignment.setRespondedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        // Try next driver or revert to searching
        autoAssignDriverForBooking(assignment.getBooking());
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
    }

    public List<DriverAssignment> getPendingAssignmentsForDriver(Long driverUserId) {
        DriverProfile profile = driverProfileRepository.findByUserId(driverUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));
        return assignmentRepository.findPendingAssignmentsForDriver(profile.getId());
    }
}
