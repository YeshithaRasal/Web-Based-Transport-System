package lk.swiftgolanka.enums;

public enum ReportType {
    RIDE_ACTIVITY("Ride Activity Report"),
    DRIVER_PERFORMANCE("Driver Performance Report"),
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
