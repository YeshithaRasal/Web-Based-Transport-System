package lk.swiftgolanka.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "driver_commissions")
public class DriverCommission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id", nullable = false)
    private DriverProfile driver;

    @Column(name = "trip_fare", nullable = false)
    private Double tripFare;

    @Column(name = "commission_rate_pct", nullable = false)
    private Double commissionRatePct;

    @Column(name = "driver_earnings", nullable = false)
    private Double driverEarnings;

    @Column(name = "platform_fee", nullable = false)
    private Double platformFee;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public DriverCommission() {}

    public DriverCommission(Long id, Trip trip, DriverProfile driver, Double tripFare, Double commissionRatePct, Double driverEarnings, Double platformFee, LocalDateTime createdAt) {
        this.id = id;
        this.trip = trip;
        this.driver = driver;
        this.tripFare = tripFare;
        this.commissionRatePct = commissionRatePct;
        this.driverEarnings = driverEarnings;
        this.platformFee = platformFee;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }

    public DriverProfile getDriver() { return driver; }
    public void setDriver(DriverProfile driver) { this.driver = driver; }

    public Double getTripFare() { return tripFare; }
    public void setTripFare(Double tripFare) { this.tripFare = tripFare; }

    public Double getCommissionRatePct() { return commissionRatePct; }
    public void setCommissionRatePct(Double commissionRatePct) { this.commissionRatePct = commissionRatePct; }

    public Double getDriverEarnings() { return driverEarnings; }
    public void setDriverEarnings(Double driverEarnings) { this.driverEarnings = driverEarnings; }

    public Double getPlatformFee() { return platformFee; }
    public void setPlatformFee(Double platformFee) { this.platformFee = platformFee; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static DriverCommissionBuilder builder() {
        return new DriverCommissionBuilder();
    }

    public static class DriverCommissionBuilder {
        private Long id;
        private Trip trip;
        private DriverProfile driver;
        private Double tripFare;
        private Double commissionRatePct;
        private Double driverEarnings;
        private Double platformFee;
        private LocalDateTime createdAt;

        public DriverCommissionBuilder id(Long id) { this.id = id; return this; }
        public DriverCommissionBuilder trip(Trip trip) { this.trip = trip; return this; }
        public DriverCommissionBuilder driver(DriverProfile driver) { this.driver = driver; return this; }
        public DriverCommissionBuilder tripFare(Double tripFare) { this.tripFare = tripFare; return this; }
        public DriverCommissionBuilder commissionRatePct(Double commissionRatePct) { this.commissionRatePct = commissionRatePct; return this; }
        public DriverCommissionBuilder driverEarnings(Double driverEarnings) { this.driverEarnings = driverEarnings; return this; }
        public DriverCommissionBuilder platformFee(Double platformFee) { this.platformFee = platformFee; return this; }
        public DriverCommissionBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public DriverCommission build() {
            return new DriverCommission(id, trip, driver, tripFare, commissionRatePct, driverEarnings, platformFee, createdAt);
        }
    }
}
