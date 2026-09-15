package lk.swiftgolanka.entity;

import jakarta.persistence.*;
import lk.swiftgolanka.enums.PaymentMethod;
import lk.swiftgolanka.enums.PaymentStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "transaction_reference", nullable = false, unique = true)
    private String transactionReference;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public Payment() {}

    public Payment(Long id, Trip trip, User passenger, Double amount, PaymentMethod paymentMethod, PaymentStatus paymentStatus, String transactionReference, LocalDateTime paidAt) {
        this.id = id;
        this.trip = trip;
        this.passenger = passenger;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus != null ? paymentStatus : PaymentStatus.PENDING;
        this.transactionReference = transactionReference;
        this.paidAt = paidAt;
    }

    @PrePersist
    protected void onCreate() {
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PENDING;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }

    public User getPassenger() { return passenger; }
    public void setPassenger(User passenger) { this.passenger = passenger; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public static PaymentBuilder builder() {
        return new PaymentBuilder();
    }

    public static class PaymentBuilder {
        private Long id;
        private Trip trip;
        private User passenger;
        private Double amount;
        private PaymentMethod paymentMethod;
        private PaymentStatus paymentStatus = PaymentStatus.PENDING;
        private String transactionReference;
        private LocalDateTime paidAt;

        public PaymentBuilder id(Long id) { this.id = id; return this; }
        public PaymentBuilder trip(Trip trip) { this.trip = trip; return this; }
        public PaymentBuilder passenger(User passenger) { this.passenger = passenger; return this; }
        public PaymentBuilder amount(Double amount) { this.amount = amount; return this; }
        public PaymentBuilder paymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public PaymentBuilder paymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; return this; }
        public PaymentBuilder transactionReference(String transactionReference) { this.transactionReference = transactionReference; return this; }
        public PaymentBuilder paidAt(LocalDateTime paidAt) { this.paidAt = paidAt; return this; }

        public Payment build() {
            return new Payment(id, trip, passenger, amount, paymentMethod, paymentStatus, transactionReference, paidAt);
        }
    }
}
