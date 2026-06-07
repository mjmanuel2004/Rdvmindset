package com.rdvmindset.config;

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
        // Active un broker simple en mémoire pour renvoyer les messages aux clients
        // Les clients vont s'abonner aux canaux commençant par "/topic"
        config.enableSimpleBroker("/topic");
        
        // Préfixe pour les messages envoyés DU client VERS le serveur (pas utilisé pour l'instant)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Le point d'entrée pour le Frontend (ex: ws://localhost:8080/ws-endpoint)
        registry.addEndpoint("/ws-endpoint")
                .setAllowedOriginPatterns("*") // Permettre à Next.js de s'y connecter
                .withSockJS(); // Fallback pour les anciens navigateurs
    }
}
