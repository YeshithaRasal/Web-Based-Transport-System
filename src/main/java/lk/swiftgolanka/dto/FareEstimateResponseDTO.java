package lk.swiftgolanka.dto;

import lk.swiftgolanka.enums.VehicleType;

public class FareEstimateResponseDTO {
    private String pickupLocation;
    private String destinationLocation;
    private VehicleType vehicleType;
    private String vehicleTypeName;
    private Double distanceKm;
    private Integer durationMin;
    private Double estimatedFare;
    private String formattedFare;

    public FareEstimateResponseDTO() {}

    public FareEstimateResponseDTO(String pickupLocation, String destinationLocation, VehicleType vehicleType, String vehicleTypeName, Double distanceKm, Integer durationMin, Double estimatedFare, String formattedFare) {
        this.pickupLocation = pickupLocation;
        this.destinationLocation = destinationLocation;
        this.vehicleType = vehicleType;
        this.vehicleTypeName = vehicleTypeName;
        this.distanceKm = distanceKm;
        this.durationMin = durationMin;
        this.estimatedFare = estimatedFare;
        this.formattedFare = formattedFare;
    }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getDestinationLocation() { return destinationLocation; }
    public void setDestinationLocation(String destinationLocation) { this.destinationLocation = destinationLocation; }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public String getVehicleTypeName() { return vehicleTypeName; }
    public void setVehicleTypeName(String vehicleTypeName) { this.vehicleTypeName = vehicleTypeName; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Integer getDurationMin() { return durationMin; }
    public void setDurationMin(Integer durationMin) { this.durationMin = durationMin; }

    public Double getEstimatedFare() { return estimatedFare; }
    public void setEstimatedFare(Double estimatedFare) { this.estimatedFare = estimatedFare; }

    public String getFormattedFare() { return formattedFare; }
    public void setFormattedFare(String formattedFare) { this.formattedFare = formattedFare; }

    public static FareEstimateResponseDTOBuilder builder() {
        return new FareEstimateResponseDTOBuilder();
    }

    public static class FareEstimateResponseDTOBuilder {
        private String pickupLocation;
        private String destinationLocation;
        private VehicleType vehicleType;
        private String vehicleTypeName;
        private Double distanceKm;
        private Integer durationMin;
        private Double estimatedFare;
        private String formattedFare;

        public FareEstimateResponseDTOBuilder pickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; return this; }
        public FareEstimateResponseDTOBuilder destinationLocation(String destinationLocation) { this.destinationLocation = destinationLocation; return this; }
        public FareEstimateResponseDTOBuilder vehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; return this; }
        public FareEstimateResponseDTOBuilder vehicleTypeName(String vehicleTypeName) { this.vehicleTypeName = vehicleTypeName; return this; }
        public FareEstimateResponseDTOBuilder distanceKm(Double distanceKm) { this.distanceKm = distanceKm; return this; }
        public FareEstimateResponseDTOBuilder durationMin(Integer durationMin) { this.durationMin = durationMin; return this; }
        public FareEstimateResponseDTOBuilder estimatedFare(Double estimatedFare) { this.estimatedFare = estimatedFare; return this; }
        public FareEstimateResponseDTOBuilder formattedFare(String formattedFare) { this.formattedFare = formattedFare; return this; }

        public FareEstimateResponseDTO build() {
            return new FareEstimateResponseDTO(pickupLocation, destinationLocation, vehicleType, vehicleTypeName, distanceKm, durationMin, estimatedFare, formattedFare);
        }
    }
}
