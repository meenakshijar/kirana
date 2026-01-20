package com.example.kirana.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final String SECRET =
            "this-is-a-very-long-secret-key-for-hs256-at-least-32-bytes";

    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();
    }


    public String generateToken(String userName, String roleId, String storeId) {

         Map<String, Object> claims = new HashMap<>();
         claims.put("role", roleId);
         claims.put("storeId", storeId);

         return Jwts.builder()
                 .setClaims(claims)
                 .setSubject(userName)
                 .setIssuedAt(new Date())
                 .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                 .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                 .compact();
     }

 }
