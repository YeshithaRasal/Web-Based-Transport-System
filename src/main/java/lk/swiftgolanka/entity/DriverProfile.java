package lk.swiftgolanka.entity;

import jakarta.persistence.*;
import lk.swiftgolanka.enums.AvailabilityStatus;
import lk.swiftgolanka.enums.VehicleType;
import lk.swiftgolanka.enums.VerificationStatus;

@Entity
@Table(name = "driver_profiles")
public class DriverProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "licence_number", nullable = false)
    private String licenceNumber;

    @Column(name = "vehicle_number", nullable = false)
    private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @Column(name = "vehicle_model")
    private String vehicleModel;

    @Column(name = "vehicle_colour")
    private String vehicleColour;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING_VERIFICATION;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false)
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.OFFLINE;

    @Column(name = "current_lat")
    private Double currentLat;

    @Column(name = "current_lng")
    private Double currentLng;

    @Column(name = "rating_avg")
    private Double ratingAvg = 5.0;

    public DriverProfile() {}

    public DriverProfile(Long id, User user, String licenceNumber, String vehicleNumber, VehicleType vehicleType, String vehicleModel, String vehicleColour, VerificationStatus verificationStatus, AvailabilityStatus availabilityStatus, Double currentLat, Double currentLng, Double ratingAvg) {
        this.id = id;
        this.user = user;
        this.licenceNumber = licenceNumber;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.vehicleModel = vehicleModel;
        this.vehicleColour = vehicleColour;
        this.verificationStatus = verificationStatus != null ? verificationStatus : VerificationStatus.PENDING_VERIFICATION;
        this.availabilityStatus = availabilityStatus != null ? availabilityStatus : AvailabilityStatus.OFFLINE;
        this.currentLat = currentLat;
        this.currentLng = currentLng;
        this.ratingAvg = ratingAvg != null ? ratingAvg : 5.0;
    }

    @PrePersist
    protected void onCreate() {
        if (verificationStatus == null) {
            verificationStatus = VerificationStatus.PENDING_VERIFICATION;
        }
        if (availabilityStatus == null) {
            availabilityStatus = AvailabilityStatus.OFFLINE;
        }
        if (ratingAvg == null) {
            ratingAvg = 5.0;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getLicenceNumber() { return licenceNumber; }
    public void setLicenceNumber(String licenceNumber) { this.licenceNumber = licenceNumber; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public String getVehicleColour() { return vehicleColour; }
    public void setVehicleColour(String vehicleColour) { this.vehicleColour = vehicleColour; }

    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public AvailabilityStatus getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) { this.availabilityStatus = availabilityStatus; }

    public Double getCurrentLat() { return currentLat; }
    public void setCurrentLat(Double currentLat) { this.currentLat = currentLat; }

    public Double getCurrentLng() { return currentLng; }
    public void setCurrentLng(Double currentLng) { this.currentLng = currentLng; }

    public Double getRatingAvg() { return ratingAvg; }
    public void setRatingAvg(Double ratingAvg) { this.ratingAvg = ratingAvg; }

    public static DriverProfileBuilder builder() {
        return new DriverProfileBuilder();
    }

    public static class DriverProfileBuilder {
        private Long id;
        private User user;
        private String licenceNumber;
        private String vehicleNumber;
        private VehicleType vehicleType;
        private String vehicleModel;
        private String vehicleColour;
        private VerificationStatus verificationStatus = VerificationStatus.PENDING_VERIFICATION;
        private AvailabilityStatus availabilityStatus = AvailabilityStatus.OFFLINE;
        private Double currentLat;
        private Double currentLng;
        private Double ratingAvg = 5.0;

        public DriverProfileBuilder id(Long id) { this.id = id; return this; }
        public DriverProfileBuilder user(User user) { this.user = user; return this; }
        public DriverProfileBuilder licenceNumber(String licenceNumber) { this.licenceNumber = licenceNumber; return this; }
        public DriverProfileBuilder vehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; return this; }
        public DriverProfileBuilder vehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; return this; }
        public DriverProfileBuilder vehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; return this; }
        public DriverProfileBuilder vehicleColour(String vehicleColour) { this.vehicleColour = vehicleColour; return this; }
        public DriverProfileBuilder verificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; return this; }
        public DriverProfileBuilder availabilityStatus(AvailabilityStatus availabilityStatus) { this.availabilityStatus = availabilityStatus; return this; }
        public DriverProfileBuilder currentLat(Double currentLat) { this.currentLat = currentLat; return this; }
        public DriverProfileBuilder currentLng(Double currentLng) { this.currentLng = currentLng; return this; }
        public DriverProfileBuilder ratingAvg(Double ratingAvg) { this.ratingAvg = ratingAvg; return this; }

        public DriverProfile build() {
            return new DriverProfile(id, user, licenceNumber, vehicleNumber, vehicleType, vehicleModel, vehicleColour, verificationStatus, availabilityStatus, currentLat, currentLng, ratingAvg);
        }
    }
}
