package com.motd.be.module.member.location.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.location.dto.response.LocationFindAllResponse;
import com.motd.be.module.member.location.service.LocationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationFacade {

	private final LocationService locationService;

	@Transactional(readOnly = true)
	public List<LocationFindAllResponse> findAll(Long locationParentId) {
		// 1. 조회
		return locationService.findAll(locationParentId);
	}
}
