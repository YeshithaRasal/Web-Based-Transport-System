package lk.swiftgolanka.service;

import lk.swiftgolanka.dto.BookingRequestDTO;
import lk.swiftgolanka.dto.FareEstimateRequestDTO;
import lk.swiftgolanka.dto.FareEstimateResponseDTO;
import lk.swiftgolanka.entity.RideBooking;
import lk.swiftgolanka.entity.User;
import lk.swiftgolanka.enums.BookingStatus;
import lk.swiftgolanka.enums.Role;
import lk.swiftgolanka.exception.InvalidOperationException;
import lk.swiftgolanka.exception.ResourceNotFoundException;
import lk.swiftgolanka.exception.UnauthorizedException;
import lk.swiftgolanka.repository.RideBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookingService {

    @Autowired
    private RideBookingRepository bookingRepository;

    @Autowired
    private MapService mapService;

    @Autowired
    private DriverAssignmentService assignmentService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public RideBooking createBooking(User passenger, BookingRequestDTO dto) {
        // Check if passenger already has an active ongoing booking
        List<RideBooking> activeBookings = bookingRepository.findActiveBookingsByPassengerId(passenger.getId());
        if (!activeBookings.isEmpty()) {
            throw new InvalidOperationException("You already have an active ride request or ongoing trip. Please complete or cancel it before booking a new one.");
        }

        if (dto.getPickupLocation() == null || dto.getPickupLocation().trim().isEmpty() ||
            dto.getDestinationLocation() == null || dto.getDestinationLocation().trim().isEmpty()) {
            throw new InvalidOperationException("Pickup and destination locations cannot be empty.");
        }
        if (dto.getPickupLocation().trim().equalsIgnoreCase(dto.getDestinationLocation().trim())) {
            throw new InvalidOperationException("Pickup and destination locations cannot be identical.");
        }

        FareEstimateRequestDTO estimateReq = new FareEstimateRequestDTO();
        estimateReq.setPickupLocation(dto.getPickupLocation());
        estimateReq.setPickupLat(dto.getPickupLat());
        estimateReq.setPickupLng(dto.getPickupLng());
        estimateReq.setDestinationLocation(dto.getDestinationLocation());
        estimateReq.setDestinationLat(dto.getDestinationLat());
        estimateReq.setDestinationLng(dto.getDestinationLng());
        estimateReq.setVehicleType(dto.getVehicleType());

        FareEstimateResponseDTO estimate = mapService.calculateFareEstimate(estimateReq);

        RideBooking booking = RideBooking.builder()
                .passenger(passenger)
                .pickupLocation(dto.getPickupLocation())
                .pickupLat(dto.getPickupLat())
                .pickupLng(dto.getPickupLng())
                .destinationLocation(dto.getDestinationLocation())
                .destinationLat(dto.getDestinationLat())
                .destinationLng(dto.getDestinationLng())
                .vehicleType(dto.getVehicleType())
                .estimatedDistanceKm(estimate.getDistanceKm())
                .estimatedDurationMin(estimate.getDurationMin())
                .estimatedFare(estimate.getEstimatedFare())
                .bookingStatus(BookingStatus.REQUESTED)
                .build();

        RideBooking savedBooking = bookingRepository.save(booking);

        notificationService.sendNotification(passenger, "Ride Requested", "Booking #" + savedBooking.getId() + " created. Please select an available driver.");
        return savedBooking;
    }

    @Transactional
    public RideBooking updateBooking(Long bookingId, BookingRequestDTO dto, User passenger) {
        RideBooking booking = getBookingById(bookingId);

        if (!booking.getPassenger().getId().equals(passenger.getId())) {
            throw new UnauthorizedException("You can only edit your own booking.");
        }

        if (booking.getBookingStatus() != BookingStatus.REQUESTED && booking.getBookingStatus() != BookingStatus.SEARCHING_DRIVER) {
            throw new InvalidOperationException("Cannot modify booking after a driver has accepted or trip has begun.");
        }

        if (dto.getPickupLocation() == null || dto.getPickupLocation().trim().isEmpty() ||
            dto.getDestinationLocation() == null || dto.getDestinationLocation().trim().isEmpty()) {
            throw new InvalidOperationException("Pickup and destination locations cannot be empty.");
        }
        if (dto.getPickupLocation().trim().equalsIgnoreCase(dto.getDestinationLocation().trim())) {
            throw new InvalidOperationException("Pickup and destination locations cannot be identical.");
        }

        FareEstimateRequestDTO estimateReq = new FareEstimateRequestDTO();
        estimateReq.setPickupLocation(dto.getPickupLocation());
        estimateReq.setPickupLat(dto.getPickupLat());
        estimateReq.setPickupLng(dto.getPickupLng());
        estimateReq.setDestinationLocation(dto.getDestinationLocation());
        estimateReq.setDestinationLat(dto.getDestinationLat());
        estimateReq.setDestinationLng(dto.getDestinationLng());
        estimateReq.setVehicleType(dto.getVehicleType());

        FareEstimateResponseDTO estimate = mapService.calculateFareEstimate(estimateReq);

        booking.setPickupLocation(dto.getPickupLocation());
        booking.setPickupLat(dto.getPickupLat());
        booking.setPickupLng(dto.getPickupLng());
        booking.setDestinationLocation(dto.getDestinationLocation());
        booking.setDestinationLat(dto.getDestinationLat());
        booking.setDestinationLng(dto.getDestinationLng());
        booking.setVehicleType(dto.getVehicleType());
        booking.setEstimatedDistanceKm(estimate.getDistanceKm());
        booking.setEstimatedDurationMin(estimate.getDurationMin());
        booking.setEstimatedFare(estimate.getEstimatedFare());

        // Cancel previous pending assignments so drivers receive updated trip info
        assignmentService.cancelPendingAssignmentsForBooking(bookingId);
        booking.setBookingStatus(BookingStatus.REQUESTED);

        notificationService.sendNotification(passenger, "Booking Updated", "Booking #" + bookingId + " route updated successfully.");
        return bookingRepository.save(booking);
    }

    public RideBooking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));
    }

    public List<RideBooking> getPassengerBookings(Long passengerId) {
        return bookingRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId);
    }

    public List<RideBooking> getPassengerActiveBookings(Long passengerId) {
        return bookingRepository.findActiveBookingsByPassengerId(passengerId);
    }

    public List<RideBooking> getAllRecentBookings() {
        return bookingRepository.findAllRecentBookings();
    }

    @Transactional
    public void cancelBooking(Long bookingId, User user) {
        RideBooking booking = getBookingById(bookingId);

        if (user.getRole() == Role.PASSENGER && !booking.getPassenger().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to cancel this booking.");
        }

        if (booking.getBookingStatus() == BookingStatus.COMPLETED || booking.getBookingStatus() == BookingStatus.IN_PROGRESS) {
            throw new InvalidOperationException("Cannot cancel a booking that is already in progress or completed.");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Clean up pending assignments sent to drivers
        assignmentService.cancelPendingAssignmentsForBooking(bookingId);

        notificationService.sendNotification(booking.getPassenger(), "Booking Cancelled", "Your ride booking #" + bookingId + " has been cancelled.");
    }
}
