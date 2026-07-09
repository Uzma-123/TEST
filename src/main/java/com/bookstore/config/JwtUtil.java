package com.bookstore.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Utility bean for generating, validating, and parsing JWT tokens.
 * Uses JJWT 0.12.x API with HS256 signing.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    // ------------------------------------------------------------------
    // Token generation
    // ------------------------------------------------------------------

    /**
     * Creates a signed JWT with the user's email as the subject.
     *
     * @param userDetails Spring Security user details
     * @return compact, URL-safe JWT string
     */
    public String generateToken(UserDetails userDetails) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // ------------------------------------------------------------------
    // Token validation
    // ------------------------------------------------------------------

    /**
     * Returns {@code true} if the token is structurally valid, properly
     * signed, not expired, and the subject matches the given user.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /** Returns {@code true} if the token's expiration is in the past. */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ------------------------------------------------------------------
    // Claims extraction
    // ------------------------------------------------------------------

    /** Extracts the {@code sub} (email) claim from the token. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
