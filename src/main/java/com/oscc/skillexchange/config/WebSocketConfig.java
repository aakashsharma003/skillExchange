package com.oscc.skillexchange.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.*;

/**
 * WebSocket configuration for real-time chat
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // WebSocketConfig.java
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Chat WebSocket endpoint
        registry.addEndpoint("/api/ws-chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        registry.addEndpoint("/api/ws-chat")
                .setAllowedOriginPatterns("*");

        // Notifications WebSocket endpoint
        registry.addEndpoint("/api/ws-notifications")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        registry.addEndpoint("/api/ws-notifications")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        log.info("Configuring message broker");

        // Enable simple broker for topics and queues
        config.enableSimpleBroker("/topic", "/queue", "/user")
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(heartBeatScheduler());

        // Prefix for messages from clients
        config.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry
                .setMessageSizeLimit(512 * 1024)      // 512 KB
                .setSendBufferSizeLimit(1024 * 1024)  // 1 MB
                .setSendTimeLimit(20 * 1000);         // 20 seconds
    }

    @Bean
    public TaskScheduler heartBeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }
}
