package com.motd.be.module.admin.director_service.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.admin.director_service.repository.DirectorServiceRepositoryForAdmin;
import com.motd.be.module.member.director_service.entity.DirectorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectorServiceCommandServiceForAdmin {

	private final DirectorServiceRepositoryForAdmin directorServiceRepositoryForAdmin;

	public DirectorService save(DirectorService directorService) {
		return directorServiceRepositoryForAdmin.save(directorService);
	}

	public void incrementSortOrder(Long id, int sortOrder, Long parentId) {
		directorServiceRepositoryForAdmin.incrementSortOrder(id, sortOrder, parentId);
	}

	public void incrementSortOrderWithStartAndEnd(int start, int end, Long parentId) {
		directorServiceRepositoryForAdmin.incrementSortOrderWithStartAndEnd(start, end, parentId);
	}

	public void decrementSortOrderWithStartAndEnd(int start, int end, Long parentId) {
		directorServiceRepositoryForAdmin.decrementSortOrderWithStartAndEnd(start, end, parentId);
	}

	public void decrementSortOrder(int sortOrder, Long parentId) {
		directorServiceRepositoryForAdmin.decrementSortOrder(sortOrder, parentId);
	}
}
