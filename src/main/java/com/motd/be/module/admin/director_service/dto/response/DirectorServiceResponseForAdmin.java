package com.motd.be.module.admin.director_service.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.List;
import java.util.stream.Collectors;

import com.motd.be.module.member.director_service.entity.DirectorService;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DirectorServiceResponseForAdmin {

	private Long id;
	private String name;
	private Long parentId;
	private String parentName;
	private Boolean isActive;
	private Boolean isDeleted;
	private Integer sortOrder;
	private String createdAt;

	public static List<DirectorServiceResponseForAdmin> fromList(List<DirectorService> directorServices) {
		return directorServices.stream()
			.map(DirectorServiceResponseForAdmin::from)
			.collect(Collectors.toList());
	}

	public static DirectorServiceResponseForAdmin from(DirectorService directorService) {
		return DirectorServiceResponseForAdmin.builder()
			.id(directorService.getId())
			.name(directorService.getName())
			.parentId(directorService.getParent() != null ? directorService.getParent().getId() : null)
			.parentName(directorService.getParent() != null ? directorService.getParent().getName() : null)
			.isActive(directorService.getIsActive())
			.isDeleted(directorService.getIsDeleted())
			.sortOrder(directorService.getSortOrder())
			.createdAt(
				directorService.getCreatedAt() != null ? formatToDateString(directorService.getCreatedAt()) : null)
			.build();
	}
}
