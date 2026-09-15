package lk.swiftgolanka.service;

import lk.swiftgolanka.dto.FareEstimateRequestDTO;
import lk.swiftgolanka.dto.FareEstimateResponseDTO;
import lk.swiftgolanka.enums.VehicleType;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;

@Service
public class MapService {

    private static final DecimalFormat df = new DecimalFormat("#.##");

    public FareEstimateResponseDTO calculateFareEstimate(FareEstimateRequestDTO request) {
        double distanceKm = 5.0; // Default fallback if no coordinates provided

        if (request.getPickupLat() != null && request.getPickupLng() != null &&
                request.getDestinationLat() != null && request.getDestinationLng() != null) {
            distanceKm = calculateHaversineDistance(
                    request.getPickupLat(), request.getPickupLng(),
                    request.getDestinationLat(), request.getDestinationLng()
            );
        } else {
            // Rough heuristic distance estimate based on text length difference for demonstration
            distanceKm = Math.max(2.5, Math.abs(request.getPickupLocation().hashCode() % 15) + 1.5);
        }

        VehicleType vehicleType = request.getVehicleType() != null ? request.getVehicleType() : VehicleType.CAR;

        // Duration estimate (approx 25 km/h urban speed in Sri Lanka)
        int durationMin = Math.max(5, (int) Math.round((distanceKm / 25.0) * 60));

        // Fare formula = Base Fare + (Distance * Rate per Km)
        double estimatedFare = Math.round(vehicleType.getBaseFare() + (distanceKm * vehicleType.getRatePerKm()));

        return FareEstimateResponseDTO.builder()
                .pickupLocation(request.getPickupLocation())
                .destinationLocation(request.getDestinationLocation())
                .vehicleType(vehicleType)
                .vehicleTypeName(vehicleType.getDisplayName())
                .distanceKm(Math.round(distanceKm * 100.0) / 100.0)
                .durationMin(durationMin)
                .estimatedFare(estimatedFare)
                .formattedFare("Rs. " + String.format("%,.2f", estimatedFare))
                .build();
    }

    public double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of Earth in KM
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
