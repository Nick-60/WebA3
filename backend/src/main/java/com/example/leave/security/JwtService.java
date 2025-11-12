package com.example.leave.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class JwtService {

    private final Key key;
    private final long expMinutes;
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.exp-minutes:120}") long expMinutes) {
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        if (raw.length < 32) {
            // ensure at least 256-bit key length for HS256
            byte[] padded = new byte[32];
            for (int i = 0; i < 32; i++) {
                padded[i] = raw[i % raw.length];
            }
            raw = padded;
        }
        this.key = Keys.hmacShaKeyFor(raw);
        this.expMinutes = expMinutes;
    }

    public String generateToken(UserDetails user, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expMinutes * 60);
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isValid(String token, UserDetails user) {
        try {
            Claims claims = parse(token);
            String subject = claims.getSubject();
            Date exp = claims.getExpiration();
            boolean ok = subject != null && subject.equals(user.getUsername()) && exp.after(new Date());
            log.debug("JWT validation: subject={}, user={}, exp={}, ok={}", subject, user.getUsername(), exp, ok);
            return ok;
        } catch (Exception e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public long getExpMinutes() {
        return expMinutes;
    }
}
