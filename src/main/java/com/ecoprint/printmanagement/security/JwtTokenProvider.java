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

<<<<<<< HEAD
import java.time.Instant;
import java.util.Arrays;
=======
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
>>>>>>> 982c1c6 (Initial commit)
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

<<<<<<< HEAD
=======
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
>>>>>>> 982c1c6 (Initial commit)
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.ecoprint.printmanagement.model.CustomUserDetails;
<<<<<<< HEAD

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenProvider {

    private static final String AUTHORITIES_CLAIM = "authorities";
    private final String jwtSecret;
    private final long jwtExpirationInMs;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String jwtSecret, @Value("${app.jwt.expiration}") long jwtExpirationInMs) {
        this.jwtSecret = jwtSecret;
=======
import com.ecoprint.printmanagement.model.token.JwtKey;
import com.ecoprint.printmanagement.repository.JwtKeyRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {
	
	@Autowired
	JwtKeyRepository jwtKeyRepository;

    private static final String AUTHORITIES_CLAIM = "authorities";
    private final long jwtExpirationInMs;

    public JwtTokenProvider(@Value("${app.jwt.expiration}") long jwtExpirationInMs) {
>>>>>>> 982c1c6 (Initial commit)
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    /**
     * Generates a token from a principal object. Embed the refresh token in the jwt
     * so that a new jwt can be created
     */
    public String generateToken(CustomUserDetails customUserDetails) {
        Instant expiryDate = Instant.now().plusMillis(jwtExpirationInMs);
        String authorities = getUserAuthorities(customUserDetails);
        return Jwts.builder()
                .setSubject(Long.toString(customUserDetails.getId()))
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(expiryDate))
<<<<<<< HEAD
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .claim(AUTHORITIES_CLAIM, authorities)
                .compact();
    }
=======
                .signWith(getSignInKey())
                .claim(AUTHORITIES_CLAIM, authorities)
                .compact();
    }
    
    public Key getSignInKey() {
    	byte[] keyBytes = null;
    	try {
			keyBytes = Decoders.BASE64.decode(getLatestKey());
		} catch (DecodingException | NoSuchAlgorithmException e) {
			e.printStackTrace();
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
>>>>>>> 982c1c6 (Initial commit)

    /**
     * Generates a token from a principal object. Embed the refresh token in the jwt
     * so that a new jwt can be created
     */
    public String generateTokenFromUserId(Long userId) {
        Instant expiryDate = Instant.now().plusMillis(jwtExpirationInMs);
        return Jwts.builder()
                .setSubject(Long.toString(userId))
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(expiryDate))
<<<<<<< HEAD
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
=======
                .signWith(getSignInKey())
>>>>>>> 982c1c6 (Initial commit)
                .compact();
    }

    /**
     * Returns the user id encapsulated within the token
     */
    public Long getUserIdFromJWT(String token) {
<<<<<<< HEAD
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
=======
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
>>>>>>> 982c1c6 (Initial commit)
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    /**
     * Returns the token expiration date encapsulated within the token
     */
    public Date getTokenExpiryFromJWT(String token) {
<<<<<<< HEAD
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
=======
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
>>>>>>> 982c1c6 (Initial commit)
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }

    /**
     * Return the jwt expiration for the client so that they can execute
     * the refresh token logic appropriately
     */
    public long getExpiryDuration() {
        return jwtExpirationInMs;
    }

    /**
     * Return the jwt authorities claim encapsulated within the token
     */
    public List<GrantedAuthority> getAuthoritiesFromJWT(String token) {
<<<<<<< HEAD
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
=======
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
>>>>>>> 982c1c6 (Initial commit)
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
