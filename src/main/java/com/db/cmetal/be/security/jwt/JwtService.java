package com.db.cmetal.be.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.db.cmetal.be.entity.Sessione;
import com.db.cmetal.be.entity.Utente;
import com.db.cmetal.be.utils.Constants;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(Utente utente) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.CLAIM_SUB, utente.getUsername());
        claims.put(Constants.CLAIM_ROLE, Constants.getRoleName(utente.getLivello()));
        return createToken(claims, jwtExpiration);
    }

    public String generateClientToken(Sessione sessione) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.CLAIM_ROLE, Constants.ROLE_CLIENT);
        claims.put(Constants.CLAIM_TAVOLO_ID, sessione.getTavolo().getId());
        claims.put(Constants.CLAIM_TAVOLO_NUM, sessione.getTavolo().getNumero());
        claims.put(Constants.CLAIM_SESSIONE_ID, sessione.getId());
        claims.put(Constants.CLAIM_IS_AYCE, sessione.getIsAyce());
        return createToken(claims, jwtExpiration);
    }

    private String createToken(Map<String, Object> claims, long expirationMillis) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = extractAllClaims(token);
        String subject = claims.getSubject();
        Date expiration = claims.getExpiration();
        return subject != null
                && subject.equals(userDetails.getUsername())
                && expiration != null
                && expiration.after(new Date());
    }
}
