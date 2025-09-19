//package com.api.matrimony.config;
//
//
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//
//import com.api.matrimony.entity.User;
//
//import java.security.Key;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Function;
//
///**
// * JWT Utility class for token generation, validation and extraction
// */
//@Component
//@Slf4j
//public class JwtUtil {
//
//    @Value("${app.jwt.secret}")
//    private String jwtSecret;
//
//    @Value("${app.jwt.expiration}")
//    private long jwtExpirationMs;
//
//    @Value("${app.jwt.refresh-expiration}")
//    private long refreshExpirationMs;
//
//    /**
//     * Generate JWT token for user
//     */
//    public String generateToken(UserDetails userDetails) {
//        Map<String, Object> claims = new HashMap<>();
//        return createToken(claims, userDetails.getUsername());
//    }
//
//    /**
//     * Generate JWT token with custom claims
//     */
//    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
//        return createToken(extraClaims, userDetails.getUsername());
//    }
//    
//	public String generateToken(User userDetails) {
//	    Map<String, Object> extraClaims = new HashMap<>();
//	    extraClaims.put("userId", userDetails.getId());
//	    extraClaims.put("userName", userDetails.getEmail());
//	    extraClaims.put("userType", (userDetails.getUserType()));
//	    extraClaims.put("role", "ROLE_" +userDetails.getUserType());
//	    return generateToken(extraClaims, userDetails);
//	}
//
//    /**
//     * Generate refresh token
//     */
//    public String generateRefreshToken(UserDetails userDetails) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("type", "refresh");
//        return createRefreshToken(claims, userDetails.getUsername());
//    }
//
//    /**
//     * Create JWT token
//     */
//    private String createToken(Map<String, Object> claims, String subject) {
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(subject)
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
//                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    /**
//     * Create refresh token
//     */
//    private String createRefreshToken(Map<String, Object> claims, String subject) {
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(subject)
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
//                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    /**
//     * Extract username from token
//     */
//    public String extractUsername(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }
//
//    /**
//     * Extract expiration date from token
//     */
//    public Date extractExpiration(String token) {
//        return extractClaim(token, Claims::getExpiration);
//    }
//
//    /**
//     * Extract specific claim from token
//     */
//    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = extractAllClaims(token);
//        return claimsResolver.apply(claims);
//    }
//
//    /**
//     * Extract all claims from token
//     */
//    private Claims extractAllClaims(String token) {
//        try {
//            return Jwts.parserBuilder()
//                    .setSigningKey(getSignInKey())
//                    .build()
//                    .parseClaimsJws(token)
//                    .getBody();
//        } catch (ExpiredJwtException e) {
//            log.error("JWT token is expired: {}", e.getMessage());
//            throw e;
//        } catch (UnsupportedJwtException e) {
//            log.error("JWT token is unsupported: {}", e.getMessage());
//            throw e;
//        } catch (MalformedJwtException e) {
//            log.error("Invalid JWT token: {}", e.getMessage());
//            throw e;
//        } catch (SignatureException e) {
//            log.error("Invalid JWT signature: {}", e.getMessage());
//            throw e;
//        } catch (IllegalArgumentException e) {
//            log.error("JWT claims string is empty: {}", e.getMessage());
//            throw e;
//        }
//    }
//
//    /**
//     * Check if token is expired
//     */
//    public Boolean isTokenExpired(String token) {
//        return extractExpiration(token).before(new Date());
//    }
//
//    /**
//     * Validate JWT token
//     */
//    public Boolean validateToken(String token, UserDetails userDetails) {
//        try {
//            final String username = extractUsername(token);
//            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
//        } catch (Exception e) {
//            log.error("JWT validation error: {}", e.getMessage());
//            return false;
//        }
//    }
//
//    /**
//     * Validate JWT token without UserDetails
//     */
//    public Boolean validateToken(String token) {
//        try {
//            extractAllClaims(token);
//            return !isTokenExpired(token);
//        } catch (Exception e) {
//            log.error("JWT validation error: {}", e.getMessage());
//            return false;
//        }
//    }
//
//    /**
//     * Get signing key for JWT
//     */
//    private Key getSignInKey() {
//        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
//        return Keys.hmacShaKeyFor(keyBytes);
//    }
//
//    /**
//     * Get expiration time in milliseconds
//     */
//    public long getExpirationTime() {
//        return jwtExpirationMs;
//    }
//
//    /**
//     * Get refresh token expiration time in milliseconds
//     */
//    public long getRefreshExpirationTime() {
//        return refreshExpirationMs;
//    }
//
//    /**
//     * Extract user ID from token if present in claims
//     */
//    public Long extractUserId(String token) {
//        Claims claims = extractAllClaims(token);
//        Object userIdClaim = claims.get("userId");
//        if (userIdClaim != null) {
//            return Long.valueOf(userIdClaim.toString());
//        }
//        return null;
//    }
//
//    /**
//     * Check if token is refresh token
//     */
//    public Boolean isRefreshToken(String token) {
//        try {
//            Claims claims = extractAllClaims(token);
//            return "refresh".equals(claims.get("type"));
//        } catch (Exception e) {
//            return false;
//        }
//    }
//}

package com.api.matrimony.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.api.matrimony.entity.User;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpirationMs;

    /**
     * Generate JWT token with automatic extra claims if UserDetails is a full User.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();

        if (userDetails instanceof User user) {
            extraClaims.put("userId", user.getId());
            extraClaims.put("userName", user.getEmail());
            extraClaims.put("userType", user.getUserType());
            extraClaims.put("role", "ROLE_" + user.getUserType());
        }

        return createToken(extraClaims, userDetails.getUsername());
    }

    /**
     * Generate JWT token with explicitly provided extra claims.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return createToken(extraClaims, userDetails.getUsername());
    }

    /**
     * Generate refresh token.
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createRefreshToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String createRefreshToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            log.error("JWT error: {}", e.getMessage());
            throw e;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public long getExpirationTime() {
        return jwtExpirationMs;
    }

    public long getRefreshExpirationTime() {
        return refreshExpirationMs;
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userIdClaim = claims.get("userId");
        if (userIdClaim != null) {
            return Long.valueOf(userIdClaim.toString());
        }
        return null;
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> extractCustomClaims(String token) {
        Claims claims = extractAllClaims(token);
        Map<String, Object> customClaims = new HashMap<>(claims);
        customClaims.remove(Claims.SUBJECT);
        customClaims.remove(Claims.ISSUED_AT);
        customClaims.remove(Claims.EXPIRATION);
        return customClaims;
    }
}
