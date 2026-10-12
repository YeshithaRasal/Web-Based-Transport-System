package lk.swiftgolanka.dto;

public class DriverIncomeSummaryDTO {
    private Long driverId;
    private String driverName;
    private String vehicleNumber;
    private String vehicleType;
    private long completedTrips;
    private double totalIncome;

    public DriverIncomeSummaryDTO() {}

    public DriverIncomeSummaryDTO(Long driverId, String driverName, String vehicleNumber, String vehicleType, long completedTrips, double totalIncome) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.completedTrips = completedTrips;
        this.totalIncome = totalIncome;
    }

    public Long getDriverId() { return driverId; }
    public String getDriverName() { return driverName; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getVehicleType() { return vehicleType; }
    public long getCompletedTrips() { return completedTrips; }
    public double getTotalIncome() { return totalIncome; }
}
