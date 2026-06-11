package com.motd.be.provider.redis.domain;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.redis.domain.repository.RedisServiceRequestExpireRepository;

@Component
public class RedisServiceRequestExpireProvider {

	@Autowired
	private RedisServiceRequestExpireRepository redisServiceRequestExpireRepository;

	public List<Long> findAll() {
		return redisServiceRequestExpireRepository.findAll();
	}

	public void save(ServiceRequest serviceRequest) {
		redisServiceRequestExpireRepository.save(serviceRequest);
	}
}
