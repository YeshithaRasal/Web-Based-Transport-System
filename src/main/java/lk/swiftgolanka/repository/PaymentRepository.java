package lk.swiftgolanka.repository;

import lk.swiftgolanka.entity.Payment;
import lk.swiftgolanka.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTripId(Long tripId);

    Optional<Payment> findByTransactionReference(String transactionReference);

    List<Payment> findByPassengerIdOrderByPaidAtDesc(Long passengerId);

    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentStatus = 'PAID'")
    Double calculateTotalRevenue();

    @Query("SELECT p FROM Payment p WHERE p.paidAt >= :startDate AND p.paidAt <= :endDate AND p.paymentStatus = 'PAID'")
    List<Payment> findPaidPaymentsInDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Payment p WHERE p.trip.driver.id = :driverProfileId ORDER BY p.paidAt DESC")
    List<Payment> findByDriverProfileId(@Param("driverProfileId") Long driverProfileId);

    @Query("SELECT COALESCE(SUM(p.amount), 0.0) FROM Payment p WHERE p.trip.driver.id = :driverProfileId AND p.paymentStatus = 'PAID'")
    Double calculateTotalIncomeForDriver(@Param("driverProfileId") Long driverProfileId);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.trip.driver.id = :driverProfileId AND p.paymentStatus = 'PAID'")
    Long countPaidTripsForDriver(@Param("driverProfileId") Long driverProfileId);
}
