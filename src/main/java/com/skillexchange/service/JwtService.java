package com.skillexchange.service;

import com.skillexchange.model.UserDetails;
import com.skillexchange.repository.AuthRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import io.jsonwebtoken.io.Decoders;

@Service
public class JwtService {

    /**
     * Base64-encoded secret. Must decode to >= 256 bits (32 bytes) for HS256.
     * Example in application.yml:
     *
     * app:
     *   jwt:
     *     secret-base64: "BASE64_STRING_HERE"
     *     ttl-minutes: 60
     *     clock-skew-seconds: 60
     */
    @Value("${app.jwt.secret-base64}")
    private String base64Secret;

    /** Token TTL in minutes (default 60). */
    @Value("${app.jwt.ttl-minutes:60}")
    private long ttlMinutes;

    /** Allowed clock skew in seconds (default 60). */
    @Value("${app.jwt.clock-skew-seconds:60}")
    private long clockSkewSeconds;

    private final AuthRepository authRepo;

    public JwtService(AuthRepository authRepo) {
        this.authRepo = authRepo;
    }

    /* ===================== TOKEN CREATION ===================== */

    public String generateToken(String email) {
        return generateToken(email, new HashMap<>());
    }

    public String generateToken(String subject, Map<String, Object> extraClaims) {
        final Date now = new Date();
        final Date exp = new Date(now.getTime() + Duration.ofMinutes(ttlMinutes).toMillis());
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put(Claims.SUBJECT, subject);
        claims.put(Claims.ISSUED_AT, now);
        claims.put(Claims.EXPIRATION, exp);
        return Jwts.builder()
                .setClaims(claims)
                .signWith(getSignKey())
                .compact();
    }

    /* ===================== EXTRACTORS ===================== */

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.getSubject());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, claims -> claims.getExpiration());
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    /* ===================== VALIDATION ===================== */

    /** Validates signature + structure + expiration. */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token); // verifies signature
            Date exp = claims.getExpiration();
            return exp != null && exp.after(new Date());
        } catch (ExpiredJwtException e) {
            return false; // expired
        } catch (JwtException | IllegalArgumentException e) {
            return false; // bad signature/malformed/unsupported
        }
    }

    /** Validates token AND ensures the subject email exists in DB. */
    public boolean validateTokenWithEmail(String token) {
        try {
            String emailFromToken = extractEmail(token);
            Optional<UserDetails> userOptional = authRepo.findByEmailIgnoreCase(emailFromToken);
            return userOptional.isPresent() && validateToken(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /* ===================== INTERNALS ===================== */

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        return Keys.hmacShaKeyFor(keyBytes); // HS256 key
    }

    private Claims extractAllClaims(String token) {
        String raw = stripBearer(token);
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(raw)
                .getBody();
    }

    private String stripBearer(String token) {
        if (token == null) return "";
        String t = token.trim();
        if (t.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return t.substring(7).trim();
        }
        return t;
    }
}