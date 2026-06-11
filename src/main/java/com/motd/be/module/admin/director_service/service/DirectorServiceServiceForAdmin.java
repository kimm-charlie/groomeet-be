package com.motd.be.module.admin.director_service.service;

import static com.motd.be.common.constants.PageSizeConstants.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.module.admin.director_service.dto.request.DirectorServiceSaveRequestForAdmin;
import com.motd.be.module.admin.director_service.dto.request.DirectorServiceUpdateRequestForAdmin;
import com.motd.be.module.admin.director_service.dto.response.DirectorServiceFindAllResponseForAdmin;
import com.motd.be.module.admin.director_service.dto.response.DirectorServiceResponseForAdmin;
import com.motd.be.module.member.director_service.entity.DirectorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectorServiceServiceForAdmin {

	private final DirectorServiceCommandServiceForAdmin directorServiceCommandServiceForAdmin;
	private final DirectorServiceQueryServiceForAdmin directorServiceQueryServiceForAdmin;

	public DirectorService save(DirectorServiceSaveRequestForAdmin request, DirectorService parent) {
		DirectorService savedDirectorService = directorServiceCommandServiceForAdmin.save(
			request.toEntity(parent));

		Long parentId = parent != null ? parent.getId() : null;
		directorServiceCommandServiceForAdmin.incrementSortOrder(savedDirectorService.getId(), request.getSortOrder(),
			parentId);

		return savedDirectorService;
	}

	public DirectorService update(Long directorServiceId, DirectorServiceUpdateRequestForAdmin request) {
		DirectorService directorService = directorServiceQueryServiceForAdmin.findById(directorServiceId);

		directorService.updateInfo(request.getName(), request.getIsActive());

		if (!directorService.getSortOrder().equals(request.getSortOrder())) {
			updateOrder(directorService, request.getSortOrder());
		}

		return directorService;
	}

	public void delete(DirectorService directorService) {
		directorService.delete();
		Long parentId = directorService.getParent() != null ? directorService.getParent().getId() : null;
		directorServiceCommandServiceForAdmin.decrementSortOrder(directorService.getSortOrder(), parentId);
	}

	public DirectorServiceFindAllResponseForAdmin findAll(int page, Boolean showIsDeleted, Long parentId) {
		Pageable pageable = PageRequest.of(page, DIRECTOR_SERVICE_PAGE_SIZE);

		Slice<DirectorService> directorServices = directorServiceQueryServiceForAdmin.findAll(pageable,
			showIsDeleted, parentId);

		return DirectorServiceFindAllResponseForAdmin.from(directorServices);
	}

	public DirectorServiceResponseForAdmin findDetail(Long directorServiceId) {
		return DirectorServiceResponseForAdmin.from(
			directorServiceQueryServiceForAdmin.findByIdIncludingDeleted(directorServiceId));
	}

	private void updateOrder(DirectorService directorService, int newOrder) {
		int originalOrder = directorService.getSortOrder();
		Long parentId = directorService.getParent() != null ? directorService.getParent().getId() : null;

		if (newOrder < originalOrder) {
			directorServiceCommandServiceForAdmin.incrementSortOrderWithStartAndEnd(newOrder, originalOrder - 1,
				parentId);
		} else if (newOrder > originalOrder) {
			directorServiceCommandServiceForAdmin.decrementSortOrderWithStartAndEnd(originalOrder + 1, newOrder,
				parentId);
		}

		directorService.updateSortOrder(newOrder);
	}
}
