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
package com.ecoprint.printmanagement.cache;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ecoprint.printmanagement.event.OnUserLogoutSuccessEvent;
import com.ecoprint.printmanagement.security.JwtTokenProvider;

import net.jodah.expiringmap.ExpiringMap;

/**
 * This cache helps maintain a state to invalidate tokens post a successful logout operation.
 * Since JWT tokens are immutable, they'd still remain accessible post logout as long as the token
 * doesn't expire.
 * 
 * Note: To prevent this cache from building up indefinitely, we set a max size. The TTL for each
 * token will be the number of seconds that remain until its expiry. This is done as an optimization
 * as once a JWT token expires, it cannot be used anyway.
 */
@Component
public class LoggedOutJwtTokenCache {

    private final ExpiringMap<String, OnUserLogoutSuccessEvent> tokenEventMap;
    private final JwtTokenProvider tokenProvider;

    @Autowired
    public LoggedOutJwtTokenCache(@Value("${app.cache.logoutToken.maxSize}") int maxSize, JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
        this.tokenEventMap = ExpiringMap.builder()
                .variableExpiration()
                .maxSize(maxSize)
                .build();
    }

    /**
     * Marks the logout event for the specified token in the cache.
     * 
     * @param event The logout event to be cached.
     */
    public void markLogoutEventForToken(OnUserLogoutSuccessEvent event) {
        String token = event.getToken();
        if (!tokenEventMap.containsKey(token)) {
            Date tokenExpiryDate = tokenProvider.getTokenExpiryFromJWT(token);
            long ttlForToken = getTTLForToken(tokenExpiryDate);
            tokenEventMap.put(token, event, ttlForToken, TimeUnit.SECONDS);
        }
    }

    /**
     * Retrieves the logout event associated with the specified token.
     * 
     * @param token The token for which to retrieve the logout event.
     * @return The logout event, or null if not found.
     */
    public OnUserLogoutSuccessEvent getLogoutEventForToken(String token) {
        return tokenEventMap.get(token);
    }

    /**
     * Calculates the time-to-live (TTL) for the token based on its expiry date.
     * 
     * @param date The expiry date of the token.
     * @return The TTL in seconds.
     */
    private long getTTLForToken(Date date) {
        long secondAtExpiry = date.toInstant().getEpochSecond();
        long secondAtLogout = Instant.now().getEpochSecond();
        return Math.max(0, secondAtExpiry - secondAtLogout);
    }
}
