package lk.swiftgolanka.repository;

import lk.swiftgolanka.entity.DriverProfile;
import lk.swiftgolanka.entity.User;
import lk.swiftgolanka.enums.AvailabilityStatus;
import lk.swiftgolanka.enums.VehicleType;
import lk.swiftgolanka.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverProfileRepository extends JpaRepository<DriverProfile, Long> {

    Optional<DriverProfile> findByUser(User user);

    Optional<DriverProfile> findByUserId(Long userId);

    List<DriverProfile> findByVerificationStatus(VerificationStatus verificationStatus);

    List<DriverProfile> findByVerificationStatusAndAvailabilityStatus(VerificationStatus verificationStatus, AvailabilityStatus availabilityStatus);

    List<DriverProfile> findByVerificationStatusAndAvailabilityStatusAndVehicleType(
            VerificationStatus verificationStatus,
            AvailabilityStatus availabilityStatus,
            VehicleType vehicleType
    );

    @Query("SELECT dp FROM DriverProfile dp WHERE dp.user.isDeleted = false AND dp.verificationStatus = 'VERIFIED'")
    List<DriverProfile> findAllVerifiedDrivers();

    @Query("SELECT dp FROM DriverProfile dp WHERE dp.user.isDeleted = false AND dp.verificationStatus = 'VERIFIED' AND dp.availabilityStatus = 'AVAILABLE'")
    List<DriverProfile> findAvailableDrivers();

    @Query("SELECT dp FROM DriverProfile dp WHERE dp.user.isDeleted = false AND dp.verificationStatus = 'VERIFIED' AND dp.availabilityStatus = 'AVAILABLE' AND dp.vehicleType = :vehicleType")
    List<DriverProfile> findAvailableDriversByVehicleType(@Param("vehicleType") VehicleType vehicleType);
}
