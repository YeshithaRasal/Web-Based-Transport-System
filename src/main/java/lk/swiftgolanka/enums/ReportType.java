package lk.swiftgolanka.enums;

public enum ReportType {
    DRIVER_PERFORMANCE("Driver Performance Report"),
    FINANCIAL_SUMMARY("Financial Summary Report"),
    TRIP_STATISTICS("Trip Statistics Report"),
    BOOKING_ANALYSIS("Booking Analysis Report"),
    CUSTOMER_FEEDBACK("Customer Feedback Report"),
    RIDE_ACTIVITY("Ride Activity Report"),
    FINANCIAL("Financial & Revenue Report"),
    CUSTOMER_SATISFACTION("Customer Satisfaction & Feedback Report"),
    OPERATIONAL_PERFORMANCE("Operational & Service Performance Report");

    private final String displayName;

    ReportType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
