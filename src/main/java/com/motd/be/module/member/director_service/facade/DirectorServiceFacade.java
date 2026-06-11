package com.motd.be.module.member.director_service.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.director_service.dto.response.DirectorServiceFindAllResponse;
import com.motd.be.module.member.director_service.service.DirectorServiceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectorServiceFacade {

	private final DirectorServiceService directorServiceService;

	public List<DirectorServiceFindAllResponse> findAll(Long parentId) {
		return directorServiceService.findAll(parentId);
	}
}
