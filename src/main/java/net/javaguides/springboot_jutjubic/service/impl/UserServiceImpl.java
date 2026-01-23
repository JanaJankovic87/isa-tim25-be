package net.javaguides.springboot_jutjubic.service.impl;

import jakarta.transaction.Transactional;
import net.javaguides.springboot_jutjubic.dto.AddressDTO;
import net.javaguides.springboot_jutjubic.model.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import net.javaguides.springboot_jutjubic.dto.UserRequest;
import net.javaguides.springboot_jutjubic.model.Role;
import net.javaguides.springboot_jutjubic.model.User;
import net.javaguides.springboot_jutjubic.repository.UserRepository;
import net.javaguides.springboot_jutjubic.service.RoleService;
import net.javaguides.springboot_jutjubic.service.UserService;
import net.javaguides.springboot_jutjubic.model.VerificationToken;
import net.javaguides.springboot_jutjubic.repository.VerificationTokenRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User save(UserRequest userRequest) {
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setFirstName(userRequest.getFirstname());
        user.setLastName(userRequest.getLastname());

        if (userRequest.getAddress() != null) {
            // Kreiraj Address i popuni lat/lng - MORA da uspe
            Address address = createAddressFromDTO(userRequest.getAddress());
            user.setAddress(address);
        }

        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setEnabled(false);
        user.setLastPasswordResetDate(new Timestamp(System.currentTimeMillis()));

        List<Role> roles = roleService.findByName("ROLE_USER");
        user.setRoles(roles);

        user = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user);
        verificationTokenRepository.save(verificationToken);

        user.setVerificationToken(token);

        return user;
    }

    @Transactional
    public boolean activateUser(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElse(null);

        if (verificationToken == null) {
            return false;
        }

        if (verificationToken.isExpired()) {
            return false;
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        user.setVerified(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);

        return true;
    }

    /**
     * Kreira Address entity iz DTO-a i popunjava lat/lng
     * Baca exception ako lokacija ne može da se odredi
     */
    private Address createAddressFromDTO(AddressDTO dto) {
        Address address = new Address();
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setPostalCode(dto.getPostalCode());
        address.setCountry(dto.getCountry());

        // Ako frontend poslao lat/lng, koristi to
        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            logger.info("Using coordinates from frontend: {}, {}", dto.getLatitude(), dto.getLongitude());
            address.setLatitude(dto.getLatitude());
            address.setLongitude(dto.getLongitude());
        } else {
            // Backend MORA da geocode-uje adresu uspešno
            logger.info("Geocoding address: {}, {}, {}", dto.getStreet(), dto.getCity(), dto.getCountry());
            geocodeAddress(address);

            // Provera da li je geocoding uspeo
            if (address.getLatitude() == null || address.getLongitude() == null) {
                throw new IllegalArgumentException("Unable to determine location for the provided address. Please provide valid address or enable location services.");
            }
        }

        return address;
    }

    /**
     * Forward geocoding - dobija lat/lng iz adrese
     * NE postavlja default - ili uspe ili baca exception
     */
    private void geocodeAddress(Address address) {
        try {
            String query = String.format("%s, %s, %s",
                    address.getStreet(),
                    address.getCity(),
                    address.getCountry()
            );

            String url = String.format(
                    "https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=1",
                    URLEncoder.encode(query, StandardCharsets.UTF_8)
            );

            logger.info("Calling Nominatim API: {}", url);

            // Nominatim zahteva User-Agent header
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Jutjubic/1.0");

            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<NominatimSearchResponse[]> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, NominatimSearchResponse[].class);

            if (response.getBody() != null && response.getBody().length > 0) {
                NominatimSearchResponse result = response.getBody()[0];
                address.setLatitude(Double.parseDouble(result.getLat()));
                address.setLongitude(Double.parseDouble(result.getLon()));
                logger.info("✓ Geocoded to: lat={}, lng={}", address.getLatitude(), address.getLongitude());
            } else {
                // Nema rezultata - NE postavlja default
                logger.error("Geocoding returned no results for address: {}", query);
                throw new IllegalArgumentException("Address not found. Please provide a valid address or enable location services.");
            }

        } catch (IllegalArgumentException e) {
            // Rethrow naš exception
            throw e;
        } catch (Exception e) {
            logger.error("Geocoding failed: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to determine location. Please try again or enable location services.");
        }
    }

    // Inner class za Nominatim search response
    public static class NominatimSearchResponse {
        private String lat;
        private String lon;
        private String display_name;

        public String getLat() { return lat; }
        public void setLat(String lat) { this.lat = lat; }

        public String getLon() { return lon; }
        public void setLon(String lon) { this.lon = lon; }

        public String getDisplay_name() { return display_name; }
        public void setDisplay_name(String display_name) { this.display_name = display_name; }
    }
}
