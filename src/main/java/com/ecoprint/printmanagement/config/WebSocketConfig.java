package com.ecoprint.printmanagement.config;



import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple, in-memory broker for broadcasting to clients on /topic
        config.enableSimpleBroker("/topic");

        // Define a prefix for application destinations to route messages to message-handling methods
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the WebSocket endpoint for client connections
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*");  // Allow all origins (adjust this for production)
    }
}



