package lk.swiftgolanka.service;

import lk.swiftgolanka.dto.PaymentRequestDTO;
import lk.swiftgolanka.enums.PaymentMethod;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentGatewayService {

    public boolean processMockPayment(PaymentRequestDTO request, double amount) {
        // Mock payment processing logic (always succeeds for valid amounts)
        if (amount <= 0) {
            return false;
        }

        if (request.getPaymentMethod() == PaymentMethod.CREDIT_CARD || request.getPaymentMethod() == PaymentMethod.DEBIT_CARD) {
            // Basic mock validation (ensure non-empty fields if card method)
            if (request.getCardNumber() != null && request.getCardNumber().replace(" ", "").length() < 12) {
                return false;
            }
        }
        
        return true; // Payment approved by mock gateway
    }

    public String generateTransactionReference() {
        return "TXN-SWIFT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
