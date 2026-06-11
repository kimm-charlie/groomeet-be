package com.motd.be.module.admin.director_service.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.admin.director_service.dto.request.DirectorServiceSaveRequestForAdmin;
import com.motd.be.module.admin.director_service.dto.request.DirectorServiceUpdateRequestForAdmin;
import com.motd.be.module.admin.director_service.dto.response.DirectorServiceFindAllResponseForAdmin;
import com.motd.be.module.admin.director_service.dto.response.DirectorServiceResponseForAdmin;
import com.motd.be.module.admin.director_service.service.DirectorServiceQueryServiceForAdmin;
import com.motd.be.module.admin.director_service.service.DirectorServiceServiceForAdmin;
import com.motd.be.module.member.director_service.entity.DirectorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectorServiceFacadeForAdmin {

	private final DirectorServiceServiceForAdmin directorServiceServiceForAdmin;
	private final DirectorServiceQueryServiceForAdmin directorServiceQueryServiceForAdmin;

	@Transactional
	public void save(DirectorServiceSaveRequestForAdmin request) {
		DirectorService parent = null;
		if (request.getParentId() != null) {
			parent = directorServiceQueryServiceForAdmin.findById(request.getParentId());
		}

		directorServiceServiceForAdmin.save(request, parent);
	}

	@Transactional
	public void update(Long directorServiceId, DirectorServiceUpdateRequestForAdmin request) {
		directorServiceServiceForAdmin.update(directorServiceId, request);
	}

	@Transactional
	public void delete(Long directorServiceId) {
		DirectorService directorService = directorServiceQueryServiceForAdmin.findById(directorServiceId);
		directorServiceServiceForAdmin.delete(directorService);
	}

	public DirectorServiceFindAllResponseForAdmin findAll(int page, Boolean showIsDeleted, Long parentId) {
		return directorServiceServiceForAdmin.findAll(page, showIsDeleted, parentId);
	}

	public DirectorServiceResponseForAdmin findDetail(Long directorServiceId) {
		return directorServiceServiceForAdmin.findDetail(directorServiceId);
	}
}
