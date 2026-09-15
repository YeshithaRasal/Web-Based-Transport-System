package lk.swiftgolanka.service;

import lk.swiftgolanka.dto.PaymentRequestDTO;
import lk.swiftgolanka.dto.RefundRequestDTO;
import lk.swiftgolanka.entity.*;
import lk.swiftgolanka.enums.PaymentStatus;
import lk.swiftgolanka.enums.RefundStatus;
import lk.swiftgolanka.exception.InvalidOperationException;
import lk.swiftgolanka.exception.ResourceNotFoundException;
import lk.swiftgolanka.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private DriverCommissionRepository commissionRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private DriverProfileRepository driverProfileRepository;

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    @Autowired
    private NotificationService notificationService;

    @Value("${swiftgolanka.app.driver-commission-pct:15.0}")
    private double platformFeeRatePct;

    @Transactional
    public Payment processPayment(PaymentRequestDTO request, User passenger) {
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + request.getTripId()));

        if (paymentRepository.findByTripId(trip.getId()).isPresent()) {
            throw new InvalidOperationException("Payment has already been processed for this trip.");
        }

        double amount = trip.getFinalFare();

        boolean isSuccess = paymentGatewayService.processMockPayment(request, amount);
        if (!isSuccess) {
            throw new InvalidOperationException("Payment processing failed. Please check card details.");
        }

        String txnRef = paymentGatewayService.generateTransactionReference();

        Payment payment = Payment.builder()
                .trip(trip)
                .passenger(passenger)
                .amount(amount)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PAID)
                .transactionReference(txnRef)
                .paidAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Calculate Driver Commission
        createDriverCommission(trip, amount);

        notificationService.sendNotification(passenger, "Payment Received", "Payment of Rs. " + amount + " successful. Ref: " + txnRef);
        notificationService.sendNotification(trip.getDriver().getUser(), "Earnings Credited", "Trip fare Rs. " + amount + " received. Net payout added to wallet.");

        return savedPayment;
    }

    private void createDriverCommission(Trip trip, double tripFare) {
        double platformFee = Math.round((tripFare * (platformFeeRatePct / 100.0)) * 100.0) / 100.0;
        double driverEarnings = tripFare - platformFee;

        DriverCommission commission = DriverCommission.builder()
                .trip(trip)
                .driver(trip.getDriver())
                .tripFare(tripFare)
                .commissionRatePct(platformFeeRatePct)
                .driverEarnings(driverEarnings)
                .platformFee(platformFee)
                .build();

        commissionRepository.save(commission);
    }

    @Transactional
    public Refund requestRefund(RefundRequestDTO request, User passenger) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found: " + request.getPaymentId()));

        if (!payment.getPassenger().getId().equals(passenger.getId())) {
            throw new InvalidOperationException("This payment does not belong to you.");
        }

        if (payment.getPaymentStatus() != PaymentStatus.PAID) {
            throw new InvalidOperationException("Only completed paid transactions can be refunded.");
        }

        Refund refund = Refund.builder()
                .payment(payment)
                .passenger(passenger)
                .amount(payment.getAmount())
                .reason(request.getReason())
                .refundStatus(RefundStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        payment.setPaymentStatus(PaymentStatus.REFUND_REQUESTED);
        paymentRepository.save(payment);

        notificationService.sendNotification(passenger, "Refund Requested", "Your refund request for Rs. " + payment.getAmount() + " has been submitted to Finance.");
        return refundRepository.save(refund);
    }

    @Transactional
    public void processRefund(Long refundId, boolean approve, String remarks, User staffUser) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund request not found: " + refundId));

        Payment payment = refund.getPayment();

        if (approve) {
            refund.setRefundStatus(RefundStatus.PROCESSED);
            refund.setProcessedAt(LocalDateTime.now());
            refund.setRemarks(remarks);

            payment.setPaymentStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            notificationService.sendNotification(refund.getPassenger(), "Refund Approved!", "Your refund of Rs. " + refund.getAmount() + " has been processed by Finance.");
        } else {
            refund.setRefundStatus(RefundStatus.REJECTED);
            refund.setProcessedAt(LocalDateTime.now());
            refund.setRemarks(remarks);

            payment.setPaymentStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);

            notificationService.sendNotification(refund.getPassenger(), "Refund Request Declined", "Your refund request was declined. Reason: " + remarks);
        }

        refundRepository.save(refund);
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
    }

    public Optional<Payment> getPaymentByTripId(Long tripId) {
        return paymentRepository.findByTripId(tripId);
    }

    public List<Payment> getPassengerPayments(Long passengerId) {
        return paymentRepository.findByPassengerIdOrderByPaidAtDesc(passengerId);
    }

    public List<DriverCommission> getDriverCommissions(Long driverUserId) {
        DriverProfile profile = driverProfileRepository.findByUserId(driverUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));
        return commissionRepository.findByDriverIdOrderByCreatedAtDesc(profile.getId());
    }

    public Double getDriverTotalEarnings(Long driverUserId) {
        DriverProfile profile = driverProfileRepository.findByUserId(driverUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));
        Double total = commissionRepository.calculateTotalEarningsForDriver(profile.getId());
        return total != null ? total : 0.0;
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Refund> getAllRefunds() {
        return refundRepository.findAll();
    }

    public Double getTotalRevenue() {
        Double rev = paymentRepository.calculateTotalRevenue();
        return rev != null ? rev : 0.0;
    }

    public Double getTotalPlatformFees() {
        Double fees = commissionRepository.calculateTotalPlatformFees();
        return fees != null ? fees : 0.0;
    }
}
