package lk.swiftgolanka.entity;

import jakarta.persistence.*;
import lk.swiftgolanka.enums.BookingStatus;
import lk.swiftgolanka.enums.VehicleType;

import java.time.LocalDateTime;

@Entity
@Table(name = "ride_bookings")
public class RideBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @Column(name = "pickup_location", nullable = false)
    private String pickupLocation;

    @Column(name = "pickup_lat")
    private Double pickupLat;

    @Column(name = "pickup_lng")
    private Double pickupLng;

    @Column(name = "destination_location", nullable = false)
    private String destinationLocation;

    @Column(name = "destination_lat")
    private Double destinationLat;

    @Column(name = "destination_lng")
    private Double destinationLng;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @Column(name = "estimated_distance_km")
    private Double estimatedDistanceKm;

    @Column(name = "estimated_duration_min")
    private Integer estimatedDurationMin;

    @Column(name = "estimated_fare")
    private Double estimatedFare;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    private BookingStatus bookingStatus = BookingStatus.REQUESTED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public RideBooking() {}

    public RideBooking(Long id, User passenger, String pickupLocation, Double pickupLat, Double pickupLng, String destinationLocation, Double destinationLat, Double destinationLng, VehicleType vehicleType, Double estimatedDistanceKm, Integer estimatedDurationMin, Double estimatedFare, BookingStatus bookingStatus, LocalDateTime createdAt) {
        this.id = id;
        this.passenger = passenger;
        this.pickupLocation = pickupLocation;
        this.pickupLat = pickupLat;
        this.pickupLng = pickupLng;
        this.destinationLocation = destinationLocation;
        this.destinationLat = destinationLat;
        this.destinationLng = destinationLng;
        this.vehicleType = vehicleType;
        this.estimatedDistanceKm = estimatedDistanceKm;
        this.estimatedDurationMin = estimatedDurationMin;
        this.estimatedFare = estimatedFare;
        this.bookingStatus = bookingStatus != null ? bookingStatus : BookingStatus.REQUESTED;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (bookingStatus == null) {
            bookingStatus = BookingStatus.REQUESTED;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getPassenger() { return passenger; }
    public void setPassenger(User passenger) { this.passenger = passenger; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public Double getPickupLat() { return pickupLat; }
    public void setPickupLat(Double pickupLat) { this.pickupLat = pickupLat; }

    public Double getPickupLng() { return pickupLng; }
    public void setPickupLng(Double pickupLng) { this.pickupLng = pickupLng; }

    public String getDestinationLocation() { return destinationLocation; }
    public void setDestinationLocation(String destinationLocation) { this.destinationLocation = destinationLocation; }

    public Double getDestinationLat() { return destinationLat; }
    public void setDestinationLat(Double destinationLat) { this.destinationLat = destinationLat; }

    public Double getDestinationLng() { return destinationLng; }
    public void setDestinationLng(Double destinationLng) { this.destinationLng = destinationLng; }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public Double getEstimatedDistanceKm() { return estimatedDistanceKm; }
    public void setEstimatedDistanceKm(Double estimatedDistanceKm) { this.estimatedDistanceKm = estimatedDistanceKm; }

    public Integer getEstimatedDurationMin() { return estimatedDurationMin; }
    public void setEstimatedDurationMin(Integer estimatedDurationMin) { this.estimatedDurationMin = estimatedDurationMin; }

    public Double getEstimatedFare() { return estimatedFare; }
    public void setEstimatedFare(Double estimatedFare) { this.estimatedFare = estimatedFare; }

    public BookingStatus getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static RideBookingBuilder builder() {
        return new RideBookingBuilder();
    }

    public static class RideBookingBuilder {
        private Long id;
        private User passenger;
        private String pickupLocation;
        private Double pickupLat;
        private Double pickupLng;
        private String destinationLocation;
        private Double destinationLat;
        private Double destinationLng;
        private VehicleType vehicleType;
        private Double estimatedDistanceKm;
        private Integer estimatedDurationMin;
        private Double estimatedFare;
        private BookingStatus bookingStatus = BookingStatus.REQUESTED;
        private LocalDateTime createdAt;

        public RideBookingBuilder id(Long id) { this.id = id; return this; }
        public RideBookingBuilder passenger(User passenger) { this.passenger = passenger; return this; }
        public RideBookingBuilder pickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; return this; }
        public RideBookingBuilder pickupLat(Double pickupLat) { this.pickupLat = pickupLat; return this; }
        public RideBookingBuilder pickupLng(Double pickupLng) { this.pickupLng = pickupLng; return this; }
        public RideBookingBuilder destinationLocation(String destinationLocation) { this.destinationLocation = destinationLocation; return this; }
        public RideBookingBuilder destinationLat(Double destinationLat) { this.destinationLat = destinationLat; return this; }
        public RideBookingBuilder destinationLng(Double destinationLng) { this.destinationLng = destinationLng; return this; }
        public RideBookingBuilder vehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; return this; }
        public RideBookingBuilder estimatedDistanceKm(Double estimatedDistanceKm) { this.estimatedDistanceKm = estimatedDistanceKm; return this; }
        public RideBookingBuilder estimatedDurationMin(Integer estimatedDurationMin) { this.estimatedDurationMin = estimatedDurationMin; return this; }
        public RideBookingBuilder estimatedFare(Double estimatedFare) { this.estimatedFare = estimatedFare; return this; }
        public RideBookingBuilder bookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; return this; }
        public RideBookingBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public RideBooking build() {
            return new RideBooking(id, passenger, pickupLocation, pickupLat, pickupLng, destinationLocation, destinationLat, destinationLng, vehicleType, estimatedDistanceKm, estimatedDurationMin, estimatedFare, bookingStatus, createdAt);
        }
    }
}
