package lk.swiftgolanka.repository;

import lk.swiftgolanka.entity.Trip;
import lk.swiftgolanka.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    Optional<Trip> findByBookingId(Long bookingId);

    List<Trip> findByPassengerIdOrderByStartTimeDesc(Long passengerId);

    List<Trip> findByDriverIdOrderByStartTimeDesc(Long driverId);

    List<Trip> findByTripStatus(TripStatus tripStatus);

    @Query("SELECT t FROM Trip t WHERE t.driver.id = :driverId AND t.tripStatus IN ('DRIVER_ARRIVING', 'ARRIVED', 'PASSENGER_PICKED_UP', 'IN_PROGRESS')")
    Optional<Trip> findActiveTripByDriverId(@Param("driverId") Long driverId);

    @Query("SELECT t FROM Trip t WHERE t.passenger.id = :passengerId AND t.tripStatus IN ('DRIVER_ARRIVING', 'ARRIVED', 'PASSENGER_PICKED_UP', 'IN_PROGRESS')")
    Optional<Trip> findActiveTripByPassengerId(@Param("passengerId") Long passengerId);

    @Query("SELECT t FROM Trip t WHERE t.startTime >= :startDate AND t.startTime <= :endDate")
    List<Trip> findTripsInDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.tripStatus = 'COMPLETED'")
    long countCompletedTrips();
}
