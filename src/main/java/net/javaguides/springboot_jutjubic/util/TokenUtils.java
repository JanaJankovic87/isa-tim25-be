package net.javaguides.springboot_jutjubic.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import net.javaguides.springboot_jutjubic.model.User;

@Component
public class TokenUtils {
    @Value("${jwt.issuer:spring-security-example}")
    private String APP_NAME;

    @Value("${jwt.secret:my-super-secure-jwt-secret-key-for-production-use-change-this-value-immediately}")
    private String SECRET;

    @Value("${jwt.expires-in.web:1800000}")
    private int EXPIRES_IN_WEB;

    @Value("${jwt.expires-in.mobile:604800000}")
    private int EXPIRES_IN_MOBILE;

    @Value("${jwt.expires-in.tablet:259200000}")
    private int EXPIRES_IN_TABLET;

    @Value("${jwt.header:Authorization}")
    private String AUTH_HEADER;

    // Tipovi uređaja
    private static final String AUDIENCE_WEB = "web";
    private static final String AUDIENCE_MOBILE = "mobile";
    private static final String AUDIENCE_TABLET = "tablet";
    private static final String AUDIENCE_UNKNOWN = "unknown";

    private static final String CLAIM_KEY_DEVICE = "device";
    private static final String CLAIM_KEY_USER_ID = "userId";
    private static final String CLAIM_KEY_EMAIL = "email";
    private static final String CLAIM_KEY_TOKEN_ID = "jti"; // JWT ID

    private SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;

    private Key getSigningKey() {
        byte[] keyBytes = SECRET.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, HttpServletRequest request) {
        String deviceType = detectDeviceType(request);
        return generateTokenForDevice(username, deviceType, null);
    }


    public String generateTokenForDevice(String username, String deviceType, User user) {
        Map<String, Object> claims = new HashMap<>();

        claims.put(CLAIM_KEY_DEVICE, deviceType);
        claims.put(CLAIM_KEY_TOKEN_ID, UUID.randomUUID().toString()); // Jedinstveni ID tokena

        if (user != null) {
            claims.put(CLAIM_KEY_USER_ID, user.getId());
            claims.put(CLAIM_KEY_EMAIL, user.getEmail());
        }

        Date now = new Date(System.currentTimeMillis());
        Date expirationDate = generateExpirationDate(deviceType);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(APP_NAME)
                .setSubject(username)
                .setAudience(deviceType)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SIGNATURE_ALGORITHM)
                .compact();
    }

    private String detectDeviceType(HttpServletRequest request) {
        if (request == null) {
            return AUDIENCE_UNKNOWN;
        }

        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return AUDIENCE_WEB;
        }

        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("mobile") ||
                userAgent.contains("android") ||
                userAgent.contains("iphone") ||
                userAgent.contains("windows phone")) {
            return AUDIENCE_MOBILE;
        }

        if (userAgent.contains("tablet") ||
                userAgent.contains("ipad")) {
            return AUDIENCE_TABLET;
        }

        return AUDIENCE_WEB;
    }

    private Date generateExpirationDate(String deviceType) {
        long expiresIn;

        switch (deviceType) {
            case AUDIENCE_MOBILE:
                expiresIn = EXPIRES_IN_MOBILE;
                break;
            case AUDIENCE_TABLET:
                expiresIn = EXPIRES_IN_TABLET;
                break;
            case AUDIENCE_WEB:
            default:
                expiresIn = EXPIRES_IN_WEB;
                break;
        }

        return new Date(System.currentTimeMillis() + expiresIn);
    }

    public String getToken(HttpServletRequest request) {
        String authHeader = getAuthHeaderFromHeader(request);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    public String getUsernameFromToken(String token) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            return claims.getSubject();
        } catch (ExpiredJwtException ex) {
            throw ex;
        } catch (Exception e) {
            return null;
        }
    }

    public Long getUserIdFromToken(String token) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            Object userId = claims.get(CLAIM_KEY_USER_ID);
            return userId != null ? Long.valueOf(userId.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }


    public String getEmailFromToken(String token) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            return (String) claims.get(CLAIM_KEY_EMAIL);
        } catch (Exception e) {
            return null;
        }
    }

    public String getDeviceTypeFromToken(String token) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            return (String) claims.get(CLAIM_KEY_DEVICE);
        } catch (Exception e) {
            return null;
        }
    }

    public String getTokenIdFromToken(String token) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            return (String) claims.get(CLAIM_KEY_TOKEN_ID);
        } catch (Exception e) {
            return null;
        }
    }


    public Date getIssuedAtDateFromToken(String token) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            return claims.getIssuedAt();
        } catch (ExpiredJwtException ex) {
            throw ex;
        } catch (Exception e) {
            return null;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            return claims.getExpiration();
        } catch (ExpiredJwtException ex) {
            throw ex;
        } catch (Exception e) {
            return null;
        }
    }

    public String getAudienceFromToken(String token) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            Object audience = claims.getAudience();
            if (audience instanceof String) {
                return (String) audience;
            }
            return null;
        } catch (ExpiredJwtException ex) {
            throw ex;
        } catch (Exception e) {
            return null;
        }
    }

    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            throw ex;
        } catch (Exception e) {
            return null;
        }
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        User user = (User) userDetails;
        final String username = getUsernameFromToken(token);
        final Date created = getIssuedAtDateFromToken(token);

        return (username != null
                && username.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && !isCreatedBeforeLastPasswordReset(created, user.getLastPasswordResetDate()));
    }

    private Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration != null && expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
        return (lastPasswordReset != null && created.before(lastPasswordReset));
    }

    public int getExpiredIn(String deviceType) {
        switch (deviceType) {
            case AUDIENCE_MOBILE:
                return EXPIRES_IN_MOBILE;
            case AUDIENCE_TABLET:
                return EXPIRES_IN_TABLET;
            case AUDIENCE_WEB:
            default:
                return EXPIRES_IN_WEB;
        }
    }

    public int getExpiredIn() {
        return EXPIRES_IN_WEB;
    }

    public String getAuthHeaderFromHeader(HttpServletRequest request) {
        return request.getHeader(AUTH_HEADER);
    }

    public Boolean isTokenForDevice(String token, String deviceType) {
        String tokenDevice = getDeviceTypeFromToken(token);
        return tokenDevice != null && tokenDevice.equals(deviceType);
    }
}
