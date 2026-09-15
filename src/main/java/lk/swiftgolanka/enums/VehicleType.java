package lk.swiftgolanka.enums;

public enum VehicleType {
    CAR("Car", 150.0, 100.0),
    VAN("Van", 250.0, 150.0),
    TUKTUK("Tuk-Tuk", 100.0, 60.0),
    BIKE("Motorcycle", 80.0, 40.0),
    SUV("Luxury SUV", 300.0, 180.0);

    private final String displayName;
    private final double baseFare;
    private final double ratePerKm;

    VehicleType(String displayName, double baseFare, double ratePerKm) {
        this.displayName = displayName;
        this.baseFare = baseFare;
        this.ratePerKm = ratePerKm;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public double getRatePerKm() {
        return ratePerKm;
    }
}
