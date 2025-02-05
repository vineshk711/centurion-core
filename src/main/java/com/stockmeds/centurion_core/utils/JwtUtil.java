package com.stockmeds.centurion_core.utils;

import com.stockmeds.centurion_core.user.dto.UserDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.stockmeds.centurion_core.constants.Constants.*;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration.min}")
    private Long expirationMin;

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String subject, Map<String, Object> jwtClaims) {
        return Jwts.builder()
                .subject(subject)
                .claims(jwtClaims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * expirationMin))
                .signWith(getSignKey())
                .compact();
    }

    public Map<String, Object> getTokenPayload(String token) {
        Claims claims = extractAllClaims(token);

        var payload = new HashMap<String, Object>();
        claims.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .forEach(entry -> payload.put(entry.getKey(), entry.getValue()));

        return Map.copyOf(payload);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}