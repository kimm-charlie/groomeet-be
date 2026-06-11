package com.motd.be.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.motd.be.common.interceptor.CookieAuthHandshakeInterceptor;
import com.motd.be.common.interceptor.StompInterceptor;
import com.motd.be.exception.handler.CustomStompErrorHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfigurer implements WebSocketMessageBrokerConfigurer {

	private final StompInterceptor stompInterceptor;
	private final CookieAuthHandshakeInterceptor cookieAuthHandshakeInterceptor;
	private final CustomStompErrorHandler customStompErrorHandler;
	@Qualifier("wsTaskScheduler")
	private final TaskScheduler wsTaskScheduler;

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// 서버 → 클라이언트
		registry.enableSimpleBroker("/sub", "/queue")
			.setTaskScheduler(wsTaskScheduler)
			.setHeartbeatValue(new long[] {5000, 5000}); // 5초 간격으로 ping/pong

		// 클라이언트 → 서버
		registry.setApplicationDestinationPrefixes("/pub");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry
			.setErrorHandler(customStompErrorHandler)
			.addEndpoint("/ws")
			.setAllowedOriginPatterns("*")
			.addInterceptors(cookieAuthHandshakeInterceptor);
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(stompInterceptor);
	}
}
