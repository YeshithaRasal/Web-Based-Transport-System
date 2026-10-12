package lk.swiftgolanka.service;

import lk.swiftgolanka.dto.PaymentRequestDTO;
import lk.swiftgolanka.enums.PaymentMethod;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentGatewayService {

    public boolean processMockPayment(PaymentRequestDTO request, double amount) {
        // Mock payment gateway automatically approves mock transactions
        return true;
    }

    public String generateTransactionReference() {
        return "TXN-SWIFT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
