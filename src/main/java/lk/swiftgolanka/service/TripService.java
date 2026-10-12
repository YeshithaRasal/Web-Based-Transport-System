package lk.swiftgolanka.service;

import lk.swiftgolanka.entity.DriverAssignment;
import lk.swiftgolanka.entity.DriverProfile;
import lk.swiftgolanka.entity.RideBooking;
import lk.swiftgolanka.entity.Trip;
import lk.swiftgolanka.entity.User;
import lk.swiftgolanka.enums.AvailabilityStatus;
import lk.swiftgolanka.enums.BookingStatus;
import lk.swiftgolanka.enums.Role;
import lk.swiftgolanka.enums.TripStatus;
import lk.swiftgolanka.exception.InvalidOperationException;
import lk.swiftgolanka.exception.ResourceNotFoundException;
import lk.swiftgolanka.repository.DriverProfileRepository;
import lk.swiftgolanka.repository.RideBookingRepository;
import lk.swiftgolanka.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TripService {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private RideBookingRepository bookingRepository;

    @Autowired
    private DriverProfileRepository driverProfileRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Trip createTripFromAssignment(DriverAssignment assignment) {
        RideBooking booking = assignment.getBooking();
        DriverProfile driver = assignment.getDriver();

        Optional<Trip> existing = tripRepository.findByBookingId(booking.getId());
        if (existing.isPresent()) {
            Trip trip = existing.get();
            trip.setDriver(driver);
            trip.setTripStatus(TripStatus.DRIVER_ARRIVING);
            if (trip.getStartTime() == null) {
                trip.setStartTime(LocalDateTime.now());
            }
            return tripRepository.save(trip);
        }

        Trip trip = Trip.builder()
                .booking(booking)
                .passenger(booking.getPassenger())
                .driver(driver)
                .pickupLocation(booking.getPickupLocation())
                .destinationLocation(booking.getDestinationLocation())
                .startTime(LocalDateTime.now())
                .distanceKm(booking.getEstimatedDistanceKm())
                .durationMin(booking.getEstimatedDurationMin())
                .finalFare(booking.getEstimatedFare())
                .tripStatus(TripStatus.DRIVER_ARRIVING)
                .build();

        return tripRepository.save(trip);
    }

    public Trip getTripById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + id));
    }

    public Optional<Trip> getActiveTripForDriver(Long driverUserId) {
        DriverProfile profile = driverProfileRepository.findByUserId(driverUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));
        return tripRepository.findActiveTripByDriverId(profile.getId());
    }

    public Optional<Trip> getActiveTripForPassenger(Long passengerId) {
        return tripRepository.findActiveTripByPassengerId(passengerId);
    }

    public List<Trip> getPassengerTripHistory(Long passengerId) {
        return tripRepository.findByPassengerIdOrderByStartTimeDesc(passengerId);
    }

    public List<Trip> getDriverTripHistory(Long driverUserId) {
        DriverProfile profile = driverProfileRepository.findByUserId(driverUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));
        return tripRepository.findByDriverIdOrderByStartTimeDesc(profile.getId());
    }

    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    @Transactional
    public void updateTripStatus(Long tripId, TripStatus newStatus, User driverUser) {
        Trip trip = getTripById(tripId);

        if (!trip.getDriver().getUser().getId().equals(driverUser.getId())) {
            throw new InvalidOperationException("You are not the driver assigned to this trip.");
        }

        if (trip.getTripStatus() == TripStatus.COMPLETED || trip.getTripStatus() == TripStatus.CANCELLED) {
            throw new InvalidOperationException("Cannot change status of a trip that is already completed or cancelled.");
        }

        if (trip.getStartTime() == null) {
            trip.setStartTime(LocalDateTime.now());
        }
        trip.setTripStatus(newStatus);

        if (newStatus == TripStatus.PASSENGER_PICKED_UP || newStatus == TripStatus.IN_PROGRESS) {
            trip.getBooking().setBookingStatus(BookingStatus.IN_PROGRESS);
            bookingRepository.save(trip.getBooking());
            notificationService.sendNotification(trip.getPassenger(), "Trip In Progress", "Your driver has confirmed pickup. Sit back and enjoy your ride!");
        } else if (newStatus == TripStatus.COMPLETED) {
            trip.setEndTime(LocalDateTime.now());
            trip.getBooking().setBookingStatus(BookingStatus.COMPLETED);
            bookingRepository.save(trip.getBooking());

            // Make driver AVAILABLE again
            DriverProfile driverProfile = trip.getDriver();
            driverProfile.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
            driverProfileRepository.save(driverProfile);

            notificationService.sendNotification(trip.getPassenger(), "Trip Completed!", "You have reached your destination. Please proceed to complete the payment.");
        } else if (newStatus == TripStatus.CANCELLED) {
            trip.setEndTime(LocalDateTime.now());
            trip.getBooking().setBookingStatus(BookingStatus.CANCELLED);
            bookingRepository.save(trip.getBooking());

            // Make driver AVAILABLE again
            DriverProfile driverProfile = trip.getDriver();
            driverProfile.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
            driverProfileRepository.save(driverProfile);

            notificationService.sendNotification(trip.getPassenger(), "Trip Cancelled", "Your trip has been cancelled.");
        }

        tripRepository.save(trip);
    }

    @Transactional
    public void adminUpdateTripStatus(Long tripId, TripStatus newStatus, User admin) {
        Trip trip = getTripById(tripId);

        if (admin.getRole() != Role.ADMIN && admin.getRole() != Role.OPERATIONS_MANAGER) {
            throw new InvalidOperationException("Unauthorized to modify trip status.");
        }

        if (trip.getTripStatus() == TripStatus.COMPLETED || trip.getTripStatus() == TripStatus.CANCELLED) {
            throw new InvalidOperationException("Cannot modify a trip that is already completed or cancelled.");
        }

        if (trip.getStartTime() == null) {
            trip.setStartTime(LocalDateTime.now());
        }
        trip.setTripStatus(newStatus);

        if (newStatus == TripStatus.COMPLETED) {
            trip.setEndTime(LocalDateTime.now());
            trip.getBooking().setBookingStatus(BookingStatus.COMPLETED);
            bookingRepository.save(trip.getBooking());

            DriverProfile driverProfile = trip.getDriver();
            driverProfile.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
            driverProfileRepository.save(driverProfile);

            notificationService.sendNotification(trip.getPassenger(), "Trip Completed by Admin", "Trip #" + tripId + " was marked completed by administrator.");
        } else if (newStatus == TripStatus.CANCELLED) {
            trip.setEndTime(LocalDateTime.now());
            trip.getBooking().setBookingStatus(BookingStatus.CANCELLED);
            bookingRepository.save(trip.getBooking());

            DriverProfile driverProfile = trip.getDriver();
            driverProfile.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
            driverProfileRepository.save(driverProfile);

            notificationService.sendNotification(trip.getPassenger(), "Trip Cancelled by Admin", "Trip #" + tripId + " was cancelled by administrator.");
        } else if (newStatus == TripStatus.PASSENGER_PICKED_UP || newStatus == TripStatus.IN_PROGRESS) {
            trip.getBooking().setBookingStatus(BookingStatus.IN_PROGRESS);
            bookingRepository.save(trip.getBooking());
        }

        tripRepository.save(trip);
    }

    @Transactional
    public void adminCancelTrip(Long tripId, User admin) {
        adminUpdateTripStatus(tripId, TripStatus.CANCELLED, admin);
    }

    @Transactional
    public void adminDeleteTrip(Long tripId, User admin) {
        Trip trip = getTripById(tripId);
        if (admin.getRole() != Role.ADMIN) {
            throw new InvalidOperationException("Only administrators can delete trip records.");
        }

        if (trip.getTripStatus() != TripStatus.COMPLETED && trip.getTripStatus() != TripStatus.CANCELLED) {
            DriverProfile driverProfile = trip.getDriver();
            driverProfile.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
            driverProfileRepository.save(driverProfile);
            trip.getBooking().setBookingStatus(BookingStatus.CANCELLED);
            bookingRepository.save(trip.getBooking());
        }

        tripRepository.delete(trip);
    }
}
