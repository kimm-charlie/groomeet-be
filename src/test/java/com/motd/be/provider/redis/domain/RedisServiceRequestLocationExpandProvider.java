package com.motd.be.provider.redis.domain;

import static com.motd.be.common.constants.Constants.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.redis.domain.repository.RedisServiceRequestLocationExpandRepository;

@Component
public class RedisServiceRequestLocationExpandProvider {

	@Autowired
	private RedisServiceRequestLocationExpandRepository redisServiceRequestLocationExpandRepository;

	public void save(ServiceRequest serviceRequest) {
		redisServiceRequestLocationExpandRepository.save(serviceRequest);
	}

	public List<Long> findExpiredRequestIds(Clock clock) {
		return redisServiceRequestLocationExpandRepository.findExpiredRequestIds(
			LocalDateTime.now(clock).atZone(KST).toEpochSecond());
	}
}
