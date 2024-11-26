/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ecoprint.printmanagement.security;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.ecoprint.printmanagement.model.CustomUserDetails;
import com.ecoprint.printmanagement.model.token.JwtKey;
import com.ecoprint.printmanagement.repository.JwtKeyRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    @Autowired
    JwtKeyRepository jwtKeyRepository;

    private static final String AUTHORITIES_CLAIM = "authorities";

    @Value("${jwt.defaultExpirationMs}")
    private long jwtExpirationInMs;

    @Value("${jwt.rememberMeExpirationMs}")
    private long rememberMeExpirationInMs;
   
    @Value("${app.jwt.secret}") 
    private String jwtSecret1;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String jwtSecret) {
        // Constructor kept for potential future use or adjustments.
    }

    /**
     * Generates a token from a principal object with a custom expiration time.
     */
    public String generateToken(CustomUserDetails customUserDetails, boolean rememberMe) {
        // Use a longer expiration time if "remember me" is enabled
        long expirationTime = rememberMe ? getExtendedExpiryDuration() : jwtExpirationInMs;
        Instant expiryDate = Instant.now().plusMillis(expirationTime);
        String authorities = getUserAuthorities(customUserDetails);

        return Jwts.builder()
                .setSubject(Long.toString(customUserDetails.getId()))
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(expiryDate))
                .signWith(getSignInKey())
                .claim(AUTHORITIES_CLAIM, authorities)
                .compact();
    }

    public long getExtendedExpiryDuration() {
        return rememberMeExpirationInMs;
    }
    
    
    public String createToken(String username, Map<String, Object> claims) {
    	
    	
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims) // Add custom claims
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + jwtExpirationInMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret1)
                .compact();
    }

    


    public Key getSignInKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(getLatestKey());
        } catch (DecodingException | NoSuchAlgorithmException e) {
            e.printStackTrace(); // Consider using a logger instead of printStackTrace for production code
            return null; // Handle error properly in production
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAndSaveKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
        keyGen.init(256);
        SecretKey secretKey = keyGen.generateKey();
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

        JwtKey jwtKey = new JwtKey();
        jwtKey.setSecretKey(encodedKey);
        jwtKey.setCreatedAt(LocalDateTime.now());
        jwtKeyRepository.save(jwtKey);

        return encodedKey;
    }

    public String getLatestKey() throws NoSuchAlgorithmException {
        JwtKey jwtKey = jwtKeyRepository.findTopByOrderByCreatedAtDesc();
        return jwtKey != null ? jwtKey.getSecretKey() : generateAndSaveKey();
    }

    /**
     * Generates a token from a principal object.
     */
    public String generateToken(CustomUserDetails customUserDetails) {
        return generateToken(customUserDetails, false);
    }

    /**
     * Generates a token from a principal object by userId.
     */
    public String generateTokenFromUserId(Long userId) {
        Instant expiryDate = Instant.now().plusMillis(jwtExpirationInMs);
        return Jwts.builder()
                .setSubject(Long.toString(userId))
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(expiryDate))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Returns the user id encapsulated within the token.
     */
    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    /**
     * Returns the token expiration date encapsulated within the token.
     */
    public Date getTokenExpiryFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }

    /**
     * Return the JWT expiration for the client so that they can execute
     * the refresh token logic appropriately.
     */
    public long getExpiryDuration() {
        return jwtExpirationInMs;
    }

    /**
     * Return the JWT authorities claim encapsulated within the token.
     */
    public List<GrantedAuthority> getAuthoritiesFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Arrays.stream(claims.get(AUTHORITIES_CLAIM).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * Private helper method to extract user authorities.
     */
    private String getUserAuthorities(CustomUserDetails customUserDetails) {
        return customUserDetails
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }
}
