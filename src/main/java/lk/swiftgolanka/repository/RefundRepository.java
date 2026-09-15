package lk.swiftgolanka.repository;

import lk.swiftgolanka.entity.Refund;
import lk.swiftgolanka.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByPassengerIdOrderByRequestedAtDesc(Long passengerId);

    List<Refund> findByPaymentId(Long paymentId);

    List<Refund> findByRefundStatus(RefundStatus refundStatus);

    @Query("SELECT SUM(r.amount) FROM Refund r WHERE r.refundStatus = 'PROCESSED'")
    Double calculateTotalRefundsProcessed();
}
