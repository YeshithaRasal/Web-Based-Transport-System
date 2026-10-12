package lk.swiftgolanka.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id", nullable = false)
    private DriverProfile driver;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    public Feedback() {}

    public Feedback(Long id, Trip trip, User passenger, DriverProfile driver, Integer rating, String comment, LocalDateTime createdAt, boolean isDeleted) {
        this.id = id;
        this.trip = trip;
        this.passenger = passenger;
        this.driver = driver;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.isDeleted = isDeleted;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }

    public User getPassenger() { return passenger; }
    public void setPassenger(User passenger) { this.passenger = passenger; }

    public DriverProfile getDriver() { return driver; }
    public void setDriver(DriverProfile driver) { this.driver = driver; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }

    public static FeedbackBuilder builder() {
        return new FeedbackBuilder();
    }

    public static class FeedbackBuilder {
        private Long id;
        private Trip trip;
        private User passenger;
        private DriverProfile driver;
        private Integer rating;
        private String comment;
        private LocalDateTime createdAt;
        private boolean isDeleted = false;

        public FeedbackBuilder id(Long id) { this.id = id; return this; }
        public FeedbackBuilder trip(Trip trip) { this.trip = trip; return this; }
        public FeedbackBuilder passenger(User passenger) { this.passenger = passenger; return this; }
        public FeedbackBuilder driver(DriverProfile driver) { this.driver = driver; return this; }
        public FeedbackBuilder rating(Integer rating) { this.rating = rating; return this; }
        public FeedbackBuilder comment(String comment) { this.comment = comment; return this; }
        public FeedbackBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public FeedbackBuilder isDeleted(boolean isDeleted) { this.isDeleted = isDeleted; return this; }

        public Feedback build() {
            return new Feedback(id, trip, passenger, driver, rating, comment, createdAt, isDeleted);
        }
    }
}
