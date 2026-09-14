/* Leaflet Map & Fare Estimation JS for Swift Go Lanka */

let map, pickupMarker, destMarker, routePolyline;

// Preset Sri Lankan Landmarks with Coordinates
const SRI_LANKA_LOCATIONS = {
    "Colombo Fort Railway Station": [6.9344, 79.8505],
    "Galle Face Green, Colombo 03": [6.9271, 79.8447],
    "Dehiwala Junction": [6.8517, 79.8647],
    "Nugegoda Flyover": [6.8649, 79.8997],
    "Katunayake BIA Airport": [7.1808, 79.8841],
    "Negombo Beach Park": [7.2285, 79.8415],
    "Kandy City Centre": [7.2906, 80.6337],
    "Galle Dutch Fort": [6.0267, 80.2170],
    "Mount Lavinia Beach": [6.8301, 79.8647]
};

function initLeafletMap(mapElementId, defaultLat = 6.9271, defaultLng = 79.8612) {
    const mapElement = document.getElementById(mapElementId);
    if (!mapElement) return null;

    map = L.map(mapElementId).setView([defaultLat, defaultLng], 12);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    return map;
}

// Calculate Haversine distance in kilometers between two lat/lng pairs
function calculateHaversineDistance(lat1, lon1, lat2, lon2) {
    const R = 6371; // Earth radius in km
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
              Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}

// Draw polyline on map
function drawRouteOnMap(pLat, pLng, dLat, dLng) {
    if (!map) return;

    if (pickupMarker) map.removeLayer(pickupMarker);
    if (destMarker) map.removeLayer(destMarker);
    if (routePolyline) map.removeLayer(routePolyline);

    pickupMarker = L.marker([pLat, pLng], {
        title: "Pickup Location"
    }).addTo(map).bindPopup("<b>Pickup:</b> " + (document.getElementById('pickupLocation')?.value || "Start Point")).openPopup();

    destMarker = L.marker([dLat, dLng], {
        title: "Destination"
    }).addTo(map).bindPopup("<b>Destination:</b> " + (document.getElementById('destinationLocation')?.value || "End Point"));

    const latlngs = [
        [pLat, pLng],
        [dLat, dLng]
    ];

    routePolyline = L.polyline(latlngs, { color: '#0d9488', weight: 5, opacity: 0.8 }).addTo(map);
    map.fitBounds(routePolyline.getBounds(), { padding: [40, 40] });
}
