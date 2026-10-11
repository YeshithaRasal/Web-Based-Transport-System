package lk.swiftgolanka.entity;

import jakarta.persistence.*;
import lk.swiftgolanka.enums.RefundStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false)
    private RefundStatus refundStatus = RefundStatus.PENDING;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(length = 1000)
    private String remarks;

    public Refund() {}

    public Refund(Long id, Payment payment, User passenger, Double amount, String reason, RefundStatus refundStatus, LocalDateTime requestedAt, LocalDateTime processedAt, String remarks) {
        this.id = id;
        this.payment = payment;
        this.passenger = passenger;
        this.amount = amount;
        this.reason = reason;
        this.refundStatus = refundStatus != null ? refundStatus : RefundStatus.PENDING;
        this.requestedAt = requestedAt != null ? requestedAt : LocalDateTime.now();
        this.processedAt = processedAt;
        this.remarks = remarks;
    }

    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
        if (refundStatus == null) {
            refundStatus = RefundStatus.PENDING;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    public User getPassenger() { return passenger; }
    public void setPassenger(User passenger) { this.passenger = passenger; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public RefundStatus getRefundStatus() { return refundStatus; }
    public void setRefundStatus(RefundStatus refundStatus) { this.refundStatus = refundStatus; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public static RefundBuilder builder() {
        return new RefundBuilder();
    }

    public static class RefundBuilder {
        private Long id;
        private Payment payment;
        private User passenger;
        private Double amount;
        private String reason;
        private RefundStatus refundStatus = RefundStatus.PENDING;
        private LocalDateTime requestedAt;
        private LocalDateTime processedAt;
        private String remarks;

        public RefundBuilder id(Long id) { this.id = id; return this; }
        public RefundBuilder payment(Payment payment) { this.payment = payment; return this; }
        public RefundBuilder passenger(User passenger) { this.passenger = passenger; return this; }
        public RefundBuilder amount(Double amount) { this.amount = amount; return this; }
        public RefundBuilder reason(String reason) { this.reason = reason; return this; }
        public RefundBuilder refundStatus(RefundStatus refundStatus) { this.refundStatus = refundStatus; return this; }
        public RefundBuilder requestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; return this; }
        public RefundBuilder processedAt(LocalDateTime processedAt) { this.processedAt = processedAt; return this; }
        public RefundBuilder remarks(String remarks) { this.remarks = remarks; return this; }

        public Refund build() {
            return new Refund(id, payment, passenger, amount, reason, refundStatus, requestedAt, processedAt, remarks);
        }
    }
}
