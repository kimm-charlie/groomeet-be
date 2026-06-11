package com.motd.be.module.member.director_info.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_info.repository.DirectorInfoJdbcRepository;
import com.motd.be.module.member.director_info.repository.DirectorInfoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectorInfoCommandService {

	private final DirectorInfoRepository directorInfoRepository;
	private final DirectorInfoJdbcRepository directorInfoJdbcRepository;

	public DirectorInfo save(DirectorInfo directorInfo) {
		return directorInfoRepository.save(directorInfo);
	}

	public void updateCompletedEstimateCountsByMembers(Map<Long, Integer> directorEstimateCountMap) {
		directorInfoJdbcRepository.updateCompletedEstimateCountsByMembers(directorEstimateCountMap);
	}
}
