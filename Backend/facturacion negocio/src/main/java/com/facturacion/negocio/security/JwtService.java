package com.facturacion.negocio.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.facturacion.negocio.domain.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final Key signingKey;
    private final long expirationMillis;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-minutes:60}") long expirationMinutes) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("jwt.secret no configurado");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = Math.max(1, expirationMinutes) * 60_000L;
    }

    public String generarToken(Usuario usuario) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(expirationMillis);
        JwtBuilder builder = Jwts.builder()
                .setSubject(usuario.getUsername())
                .claim("uid", usuario.getId())
                .claim("rol", usuario.getRol().name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(signingKey, SignatureAlgorithm.HS256);
        if (usuario.getNegocio() != null && usuario.getNegocio().getId() != null) {
            builder.claim("negocioId", usuario.getNegocio().getId());
        }
        return builder.compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long calcularExpiracionEpochMillis() {
        return Instant.now().plusMillis(expirationMillis).toEpochMilli();
    }

    public Long extractLongClaim(Claims claims, String claimName) {
        Object value = claims.get(claimName);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}
