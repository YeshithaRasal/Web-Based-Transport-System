package lk.swiftgolanka.enums;

public enum PaymentMethod {
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    CASH("Cash Payment"),
    DIGITAL_WALLET("Digital Wallet / LankaPay");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
