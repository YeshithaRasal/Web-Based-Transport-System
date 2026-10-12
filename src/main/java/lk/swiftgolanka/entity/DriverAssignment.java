package lk.swiftgolanka.entity;

import jakarta.persistence.*;
import lk.swiftgolanka.enums.AssignmentStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "driver_assignments")
public class DriverAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "booking_id", nullable = false)
    private RideBooking booking;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id", nullable = false)
    private DriverProfile driver;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_status", nullable = false)
    private AssignmentStatus assignmentStatus = AssignmentStatus.PENDING;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    public DriverAssignment() {}

    public DriverAssignment(Long id, RideBooking booking, DriverProfile driver, AssignmentStatus assignmentStatus, LocalDateTime assignedAt, LocalDateTime respondedAt) {
        this.id = id;
        this.booking = booking;
        this.driver = driver;
        this.assignmentStatus = assignmentStatus != null ? assignmentStatus : AssignmentStatus.PENDING;
        this.assignedAt = assignedAt != null ? assignedAt : LocalDateTime.now();
        this.respondedAt = respondedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
        if (assignmentStatus == null) {
            assignmentStatus = AssignmentStatus.PENDING;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RideBooking getBooking() { return booking; }
    public void setBooking(RideBooking booking) { this.booking = booking; }

    public DriverProfile getDriver() { return driver; }
    public void setDriver(DriverProfile driver) { this.driver = driver; }

    public AssignmentStatus getAssignmentStatus() { return assignmentStatus; }
    public void setAssignmentStatus(AssignmentStatus assignmentStatus) { this.assignmentStatus = assignmentStatus; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }

    public static DriverAssignmentBuilder builder() {
        return new DriverAssignmentBuilder();
    }

    public static class DriverAssignmentBuilder {
        private Long id;
        private RideBooking booking;
        private DriverProfile driver;
        private AssignmentStatus assignmentStatus = AssignmentStatus.PENDING;
        private LocalDateTime assignedAt;
        private LocalDateTime respondedAt;

        public DriverAssignmentBuilder id(Long id) { this.id = id; return this; }
        public DriverAssignmentBuilder booking(RideBooking booking) { this.booking = booking; return this; }
        public DriverAssignmentBuilder driver(DriverProfile driver) { this.driver = driver; return this; }
        public DriverAssignmentBuilder assignmentStatus(AssignmentStatus assignmentStatus) { this.assignmentStatus = assignmentStatus; return this; }
        public DriverAssignmentBuilder assignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; return this; }
        public DriverAssignmentBuilder respondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; return this; }

        public DriverAssignment build() {
            return new DriverAssignment(id, booking, driver, assignmentStatus, assignedAt, respondedAt);
        }
    }
}
