package lk.swiftgolanka.entity;

import jakarta.persistence.*;
import lk.swiftgolanka.enums.TripStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "booking_id", nullable = false)
    private RideBooking booking;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id", nullable = false)
    private DriverProfile driver;

    @Column(name = "pickup_location", nullable = false)
    private String pickupLocation;

    @Column(name = "destination_location", nullable = false)
    private String destinationLocation;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "duration_min")
    private Integer durationMin;

    @Column(name = "final_fare")
    private Double finalFare;

    @Enumerated(EnumType.STRING)
    @Column(name = "trip_status", nullable = false)
    private TripStatus tripStatus = TripStatus.DRIVER_ARRIVING;

    public Trip() {}

    public Trip(Long id, RideBooking booking, User passenger, DriverProfile driver, String pickupLocation, String destinationLocation, LocalDateTime startTime, LocalDateTime endTime, Double distanceKm, Integer durationMin, Double finalFare, TripStatus tripStatus) {
        this.id = id;
        this.booking = booking;
        this.passenger = passenger;
        this.driver = driver;
        this.pickupLocation = pickupLocation;
        this.destinationLocation = destinationLocation;
        this.startTime = startTime;
        this.endTime = endTime;
        this.distanceKm = distanceKm;
        this.durationMin = durationMin;
        this.finalFare = finalFare;
        this.tripStatus = tripStatus != null ? tripStatus : TripStatus.DRIVER_ARRIVING;
    }

    @PrePersist
    protected void onCreate() {
        if (tripStatus == null) {
            tripStatus = TripStatus.DRIVER_ARRIVING;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RideBooking getBooking() { return booking; }
    public void setBooking(RideBooking booking) { this.booking = booking; }

    public User getPassenger() { return passenger; }
    public void setPassenger(User passenger) { this.passenger = passenger; }

    public DriverProfile getDriver() { return driver; }
    public void setDriver(DriverProfile driver) { this.driver = driver; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getDestinationLocation() { return destinationLocation; }
    public void setDestinationLocation(String destinationLocation) { this.destinationLocation = destinationLocation; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Integer getDurationMin() { return durationMin; }
    public void setDurationMin(Integer durationMin) { this.durationMin = durationMin; }

    public Double getFinalFare() { return finalFare; }
    public void setFinalFare(Double finalFare) { this.finalFare = finalFare; }

    public TripStatus getTripStatus() { return tripStatus; }
    public void setTripStatus(TripStatus tripStatus) { this.tripStatus = tripStatus; }

    public static TripBuilder builder() {
        return new TripBuilder();
    }

    public static class TripBuilder {
        private Long id;
        private RideBooking booking;
        private User passenger;
        private DriverProfile driver;
        private String pickupLocation;
        private String destinationLocation;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Double distanceKm;
        private Integer durationMin;
        private Double finalFare;
        private TripStatus tripStatus = TripStatus.DRIVER_ARRIVING;

        public TripBuilder id(Long id) { this.id = id; return this; }
        public TripBuilder booking(RideBooking booking) { this.booking = booking; return this; }
        public TripBuilder passenger(User passenger) { this.passenger = passenger; return this; }
        public TripBuilder driver(DriverProfile driver) { this.driver = driver; return this; }
        public TripBuilder pickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; return this; }
        public TripBuilder destinationLocation(String destinationLocation) { this.destinationLocation = destinationLocation; return this; }
        public TripBuilder startTime(LocalDateTime startTime) { this.startTime = startTime; return this; }
        public TripBuilder endTime(LocalDateTime endTime) { this.endTime = endTime; return this; }
        public TripBuilder distanceKm(Double distanceKm) { this.distanceKm = distanceKm; return this; }
        public TripBuilder durationMin(Integer durationMin) { this.durationMin = durationMin; return this; }
        public TripBuilder finalFare(Double finalFare) { this.finalFare = finalFare; return this; }
        public TripBuilder tripStatus(TripStatus tripStatus) { this.tripStatus = tripStatus; return this; }

        public Trip build() {
            return new Trip(id, booking, passenger, driver, pickupLocation, destinationLocation, startTime, endTime, distanceKm, durationMin, finalFare, tripStatus);
        }
    }
}
