package com.motd.be.redis.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import com.motd.be.redis.domain.brocker.ChatMessageSubscriber;
import com.motd.be.redis.domain.brocker.SseEventSubscriber;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RedisPubSubConfig {

	private final SseEventSubscriber sseEventSubscriber;
	private final ChatMessageSubscriber chatMessageSubscriber;

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);

		// 구독 채널 등록
		container.addMessageListener(sseEventSubscriber, new ChannelTopic("sse:event"));

		// 채팅 메시지용
		container.addMessageListener(chatMessageSubscriber, new ChannelTopic("chat:message"));
		return container;
	}

	@Bean
	public ChannelTopic sseChannelTopic() {
		return new ChannelTopic("sse:event");
	}

	@Bean
	public ChannelTopic chatChannelTopic() {
		return new ChannelTopic("chat:message");
	}

}
