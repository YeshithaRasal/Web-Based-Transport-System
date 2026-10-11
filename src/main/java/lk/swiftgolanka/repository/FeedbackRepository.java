package lk.swiftgolanka.repository;

import lk.swiftgolanka.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Optional<Feedback> findByTripIdAndIsDeletedFalse(Long tripId);

    List<Feedback> findByDriverIdAndIsDeletedFalseOrderByCreatedAtDesc(Long driverId);

    List<Feedback> findByPassengerIdAndIsDeletedFalseOrderByCreatedAtDesc(Long passengerId);

    List<Feedback> findByIsDeletedFalseOrderByCreatedAtDesc();

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.driver.id = :driverId AND f.isDeleted = false")
    Double calculateAverageRatingForDriver(@Param("driverId") Long driverId);
}
