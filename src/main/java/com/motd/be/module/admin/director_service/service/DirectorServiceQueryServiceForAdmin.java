package com.motd.be.module.admin.director_service.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.DirectorServiceException;
import com.motd.be.module.admin.director_service.repository.DirectorServiceQueryDslRepositoryForAdmin;
import com.motd.be.module.admin.director_service.repository.DirectorServiceRepositoryForAdmin;
import com.motd.be.module.member.director_service.entity.DirectorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectorServiceQueryServiceForAdmin {

	private final DirectorServiceRepositoryForAdmin directorServiceRepositoryForAdmin;
	private final DirectorServiceQueryDslRepositoryForAdmin directorServiceQueryDslRepositoryForAdmin;

	public DirectorService findById(Long directorServiceId) {
		return directorServiceRepositoryForAdmin.findByIdAndIsDeletedFalse(directorServiceId)
			.orElseThrow(() -> new CustomRuntimeException(DirectorServiceException.NOT_FOUND));
	}

	public DirectorService findByIdIncludingDeleted(Long directorServiceId) {
		return directorServiceRepositoryForAdmin.findById(directorServiceId)
			.orElseThrow(() -> new CustomRuntimeException(DirectorServiceException.NOT_FOUND));
	}

	public Slice<DirectorService> findAll(Pageable pageable, Boolean showIsDeleted, Long parentId) {
		return directorServiceQueryDslRepositoryForAdmin.findAll(pageable, showIsDeleted, parentId);
	}
}
