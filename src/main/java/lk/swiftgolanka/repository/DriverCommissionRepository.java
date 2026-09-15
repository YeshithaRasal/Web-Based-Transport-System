package lk.swiftgolanka.repository;

import lk.swiftgolanka.entity.DriverCommission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverCommissionRepository extends JpaRepository<DriverCommission, Long> {

    Optional<DriverCommission> findByTripId(Long tripId);

    List<DriverCommission> findByDriverIdOrderByCreatedAtDesc(Long driverId);

    @Query("SELECT SUM(dc.driverEarnings) FROM DriverCommission dc WHERE dc.driver.id = :driverId")
    Double calculateTotalEarningsForDriver(@Param("driverId") Long driverId);

    @Query("SELECT SUM(dc.platformFee) FROM DriverCommission dc")
    Double calculateTotalPlatformFees();
}
