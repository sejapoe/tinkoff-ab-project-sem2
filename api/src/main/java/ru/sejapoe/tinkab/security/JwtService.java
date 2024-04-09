package ru.sejapoe.tinkab.security;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sejapoe.tinkab.config.JwtProperties;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;
    private SecretKey secretKey;
    private JwtParser jwtParser;

    @PostConstruct
    private void init() {
        secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        jwtParser = Jwts.parser().verifyWith(secretKey).build();
    }

    public String generateToken(Map<String, Object> extraClaims, String subject, Duration duration) {
        var claims = new HashMap<>(extraClaims);
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + duration.toMillis()))
                .issuer(jwtProperties.issuer())
                .signWith(secretKey)
                .compact();
    }

    public String extractSubject(String token) {
        return jwtParser.parseSignedClaims(token).getPayload().getSubject();
    }
}
