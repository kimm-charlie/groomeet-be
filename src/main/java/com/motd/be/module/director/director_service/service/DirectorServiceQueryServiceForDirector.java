package com.motd.be.module.director.director_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.DirectorServiceException;
import com.motd.be.module.director.director_service.repository.DirectorServiceRepositoryForDirector;
import com.motd.be.module.member.director_service.entity.DirectorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectorServiceQueryServiceForDirector {

	private final DirectorServiceRepositoryForDirector directorServiceRepositoryForDirector;

	public DirectorService findById(Long id) {
		return directorServiceRepositoryForDirector.findById(id)
			.orElseThrow(() -> new CustomRuntimeException(DirectorServiceException.NOT_FOUND));
	}

	public List<DirectorService> findAllByIds(List<Long> ids) {
		return directorServiceRepositoryForDirector.findAllByIds(ids);
	}
}
