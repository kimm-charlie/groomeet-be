package com.motd.be.provider.redis.domain;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.redis.domain.repository.RedisDirectorHideRequestRepository;

@Component
public class RedisDirectorHideRequestProvider {

	@Autowired
	private RedisDirectorHideRequestRepository redisDirectorHideRequestRepository;

	public void save(DirectorInfo directorInfo, Long serviceRequestId) {
		redisDirectorHideRequestRepository.hideRequest(directorInfo.getId(), serviceRequestId);
	}

	public Set<Long> findAll(DirectorInfo directorInfo) {
		return redisDirectorHideRequestRepository.findAllHiddenRequests(directorInfo.getId());
	}
}
