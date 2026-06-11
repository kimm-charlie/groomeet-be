package com.motd.be.redis.domain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "redis")
@Getter
@Setter
public class RedisProperties {

	private int port;
	private String host;
}
