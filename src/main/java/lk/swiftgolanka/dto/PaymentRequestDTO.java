package lk.swiftgolanka.dto;

import jakarta.validation.constraints.NotNull;
import lk.swiftgolanka.enums.PaymentMethod;

public class PaymentRequestDTO {

    @NotNull(message = "Trip ID is required")
    private Long tripId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String cardNumber;
    private String cardExpiry;
    private String cardCvv;
    private String cardHolderName;

    public PaymentRequestDTO() {}

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getCardExpiry() { return cardExpiry; }
    public void setCardExpiry(String cardExpiry) { this.cardExpiry = cardExpiry; }

    public String getCardCvv() { return cardCvv; }
    public void setCardCvv(String cardCvv) { this.cardCvv = cardCvv; }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }
}
