package net.javaguides.springboot_jutjubic.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import net.javaguides.springboot_jutjubic.dto.LocationDTO;
import net.javaguides.springboot_jutjubic.model.Address;

@Service
public class GeolocationService {

    private static final Logger logger = LoggerFactory.getLogger(GeolocationService.class);
    private final RestTemplate restTemplate;

    public GeolocationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public LocationDTO getLocationFromIP(String ipAddress) {
        // Localhost ili private IP
        if (isLocalIP(ipAddress)) {
            logger.info("✓ Local IP detected: {}, using Belgrade for testing", ipAddress);
            LocationDTO location = new LocationDTO(44.7866, 20.4489, true);
            location.setLocationName("Belgrade, Serbia");
            return location;
        }

        // Pravi javni IP → pozovi API
        try {
            String url = String.format("https://ipapi.co/%s/json/", ipAddress);
            logger.info("Fetching geolocation for IP: {}", ipAddress);

            IpApiResponse response = restTemplate.getForObject(url, IpApiResponse.class);

            if (response != null && response.getLatitude() != null) {
                LocationDTO location = new LocationDTO(
                        response.getLatitude(),
                        response.getLongitude(),
                        true
                );
                location.setLocationName(response.getCity() + ", " + response.getCountry());
                logger.info("✓ Location resolved from IP: {}", location.getLocationName());
                return location;
            }
        } catch (Exception e) {
            logger.error("IP geolocation API failed: {}", e.getMessage());
        }

        // Fallback ako API ne radi
        logger.warn("IP geolocation failed, using Belgrade as fallback");
        LocationDTO location = new LocationDTO(44.7866, 20.4489, true);
        location.setLocationName("Belgrade, Serbia");
        return location;
    }

    private boolean isLocalIP(String ip) {
        return ip == null || ip.isEmpty() ||
                ip.equals("127.0.0.1") ||
                ip.equals("::1") ||
                ip.equals("0:0:0:0:0:0:0:1") ||
                ip.startsWith("192.168.") ||
                ip.startsWith("10.") ||
                ip.startsWith("172.16.") ||
                ip.startsWith("172.17.") ||
                ip.startsWith("172.18.") ||
                ip.startsWith("172.19.") ||
                ip.startsWith("172.20.") ||
                ip.startsWith("172.21.") ||
                ip.startsWith("172.22.") ||
                ip.startsWith("172.23.") ||
                ip.startsWith("172.24.") ||
                ip.startsWith("172.25.") ||
                ip.startsWith("172.26.") ||
                ip.startsWith("172.27.") ||
                ip.startsWith("172.28.") ||
                ip.startsWith("172.29.") ||
                ip.startsWith("172.30.") ||
                ip.startsWith("172.31.");
    }

    public static class IpApiResponse {
        private Double latitude;
        private Double longitude;
        private String city;
        private String country;

        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }

        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }
}