package lk.swiftgolanka.repository;

import lk.swiftgolanka.entity.RideBooking;
import lk.swiftgolanka.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RideBookingRepository extends JpaRepository<RideBooking, Long> {

    List<RideBooking> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);

    List<RideBooking> findByBookingStatus(BookingStatus bookingStatus);

    List<RideBooking> findByBookingStatusIn(List<BookingStatus> statuses);

    @Query("SELECT rb FROM RideBooking rb WHERE rb.passenger.id = :passengerId AND rb.bookingStatus IN ('REQUESTED', 'SEARCHING_DRIVER', 'DRIVER_ASSIGNED', 'IN_PROGRESS') ORDER BY rb.createdAt DESC")
    List<RideBooking> findActiveBookingsByPassengerId(@Param("passengerId") Long passengerId);

    @Query("SELECT rb FROM RideBooking rb ORDER BY rb.createdAt DESC")
    List<RideBooking> findAllRecentBookings();
}
