package com.skillexchange.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // STOMP endpoints clients connect to
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // raw WebSocket
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*");

        // SockJS fallback (optional for browsers behind proxies/firewalls)
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    // STOMP routing (app destinations + broker destinations)
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // in-memory simple broker for topics/queues
        config.enableSimpleBroker("/topic", "/queue")
              .setHeartbeatValue(new long[]{10000, 10000}) // 10s inbound/outbound
              .setTaskScheduler(heartbeatScheduler());
        // messages sent by clients to @MessageMapping handlers must start with /app
        config.setApplicationDestinationPrefixes("/app");
        // if you ever use convertAndSendToUser(), uncomment:
        // config.setUserDestinationPrefix("/user");
    }

    // Optional: tune transport limits (large payloads, etc.)
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry
            .setMessageSizeLimit(256 * 1024)       // 256 KB
            .setSendBufferSizeLimit(512 * 1024)    // 512 KB
            .setSendTimeLimit(15 * 1000);          // 15s
    }

    // scheduler needed for broker heartbeats
    @Bean
    public TaskScheduler heartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("stomp-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }
}

