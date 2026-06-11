package com.motd.be.redis.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableRedisRepositories(enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
public class RedisConfig {

	private final RedisProperties redisProperties;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory(redisProperties.getHost(), redisProperties.getPort());
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.activateDefaultTyping(
			LaissezFaireSubTypeValidator.instance,
			ObjectMapper.DefaultTyping.NON_FINAL,
			JsonTypeInfo.As.PROPERTY
		);

		GenericJackson2JsonRedisSerializer serializer =
			new GenericJackson2JsonRedisSerializer(objectMapper);

		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(serializer);
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(serializer);

		return template;
	}

	@Bean
	public RedisTemplate<String, Long> redisLongTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Long> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		// Key
		template.setKeySerializer(new StringRedisSerializer());

		// 일반 Value
		template.setValueSerializer(new GenericToStringSerializer<>(Long.class));

		// Hash Key
		template.setHashKeySerializer(new StringRedisSerializer());

		// Hash Value (숫자 연산 가능하게 설정)
		template.setHashValueSerializer(new GenericToStringSerializer<>(Long.class));
		return template;
	}

	@Bean
	public RedisTemplate<String, String> redisStringTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(new StringRedisSerializer());
		return template;
	}
}
