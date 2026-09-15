package lk.swiftgolanka.repository;

import lk.swiftgolanka.entity.Complaint;
import lk.swiftgolanka.enums.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Complaint> findByComplaintStatusOrderByCreatedAtDesc(ComplaintStatus complaintStatus);

    List<Complaint> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.complaintStatus = 'OPEN' OR c.complaintStatus = 'IN_PROGRESS'")
    long countActiveComplaints();
}
