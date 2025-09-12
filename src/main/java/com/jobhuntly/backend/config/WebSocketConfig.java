package com.jobhuntly.backend.config;

import com.jobhuntly.backend.websocket.WsHandshakeAuthInterceptor;
import com.jobhuntly.backend.websocket.WsHandshakeHandler;
import com.jobhuntly.backend.websocket.WsSubscribeGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final WsHandshakeAuthInterceptor wsHandshakeAuthInterceptor;
    private final WsHandshakeHandler wsHandshakeHandler;
    private final WsSubscribeGuard wsSubscribeGuard;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // SockJS
        registry.addEndpoint("/ws")
                .setHandshakeHandler(wsHandshakeHandler)
                .addInterceptors(wsHandshakeAuthInterceptor)
                .setAllowedOriginPatterns(
                        "http://localhost:3000",
                        "http://18.142.226.139:3000",
                        "http://47.129.60.85:3000",
                        "http://localhost:5173",
                        "http://jobhuntly.io.vn",
                        "http://admin.jobhuntly.io.vn",
                        "https://jobhuntly.io.vn",
                        "https://admin.jobhuntly.io.vn"
                )
                .withSockJS()
                .setSessionCookieNeeded(true);


        // Native WS
        registry.addEndpoint("/ws")
                .setHandshakeHandler(wsHandshakeHandler)
                .addInterceptors(wsHandshakeAuthInterceptor)
                .setAllowedOriginPatterns(
                        "http://localhost:3000",
                        "http://18.142.226.139:3000",
                        "http://47.129.60.85:3000",
                        "http://localhost:5173",
                        "http://jobhuntly.io.vn",
                        "http://admin.jobhuntly.io.vn",
                        "https://jobhuntly.io.vn",
                        "https://admin.jobhuntly.io.vn"
                );
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
        registry.enableSimpleBroker("/topic", "/queue")
                .setTaskScheduler(heartBeatScheduler())
                .setHeartbeatValue(new long[]{10000, 10000});
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(wsSubscribeGuard);
    }

    @Bean
    public ThreadPoolTaskScheduler heartBeatScheduler() {
        ThreadPoolTaskScheduler ts = new ThreadPoolTaskScheduler();
        ts.setPoolSize(1);
        ts.setThreadNamePrefix("ws-heartbeat-");
        ts.initialize();
        return ts;
    }
}
