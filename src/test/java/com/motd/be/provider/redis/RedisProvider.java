package com.motd.be.provider.redis;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisProvider {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	public void flushDB() {
		Set<String> keys = redisTemplate.keys("*");
		if (keys != null && !keys.isEmpty()) {
			redisTemplate.delete(keys);
		}
	}
}
