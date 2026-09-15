package lk.swiftgolanka.repository;

import lk.swiftgolanka.entity.DriverAssignment;
import lk.swiftgolanka.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverAssignmentRepository extends JpaRepository<DriverAssignment, Long> {

    List<DriverAssignment> findByBookingId(Long bookingId);

    List<DriverAssignment> findByDriverId(Long driverId);

    List<DriverAssignment> findByDriverIdAndAssignmentStatus(Long driverId, AssignmentStatus assignmentStatus);

    List<DriverAssignment> findByDriverIdOrderByAssignedAtDesc(Long driverId);

    @Query("SELECT da FROM DriverAssignment da WHERE da.driver.id = :driverId AND da.assignmentStatus = 'PENDING' ORDER BY da.assignedAt DESC")
    List<DriverAssignment> findPendingAssignmentsForDriver(@Param("driverId") Long driverId);

    Optional<DriverAssignment> findByBookingIdAndDriverId(Long bookingId, Long driverId);
}
