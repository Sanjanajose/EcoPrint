package com.ecoprint.printmanagement.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, 
                                   ServerHttpResponse response, 
                                   WebSocketHandler wsHandler, 
                                   Map<String, Object> attributes) throws Exception {
        // Extract the token from either query parameters or headers
        String token = extractToken(request);

        // Validate the token; if valid, continue the handshake
        if (validateToken(token)) {
            // Create a Principal based on the token to associate with the WebSocket session
            Principal userPrincipal = () -> token;

            // Save the principal to the session attributes for access in WebSocket handlers
            attributes.put("user", userPrincipal);
            return true;  // Allow the WebSocket connection
        }

        // Deny the handshake if the token is invalid
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, 
                               ServerHttpResponse response, 
                               WebSocketHandler wsHandler, 
                               Exception exception) {
        // No actions required post-handshake in this case
    }

    /**
     * Extracts the token from query parameters or the Authorization header.
     */
    private String extractToken(ServerHttpRequest request) {
        // Check if the token is available as a query parameter
        String token = extractTokenFromQuery(request.getURI().getQuery());

        // If not found in query, check the Authorization header
        if (token == null) {
            String authorizationHeader = request.getHeaders().getFirst("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);  // Strip "Bearer " prefix
            }
        }

        return token;
    }

    /**
     * Parses the query string to locate an 'access_token' parameter.
     */
    private String extractTokenFromQuery(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }

        for (String param : query.split("&")) {
            if (param.startsWith("access_token=")) {
                return param.substring(13);  // Extract the token value
            }
        }
        return null;
    }

    /**
     * Validates the extracted token. Replace this with actual JWT validation logic.
     */
    private boolean validateToken(String token) {
        // Simple check for a non-null, non-empty token; replace with JWT validation as needed
        return token != null && !token.isEmpty();
        
        /* Example of JWT validation logic:
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)  // Replace 'secretKey' with your actual key
                    .parseClaimsJws(token)
                    .getBody();
            return true;  // Valid token if no exception is thrown
        } catch (JwtException | IllegalArgumentException e) {
            return false;  // Invalid token
        }
        */
    }
}
