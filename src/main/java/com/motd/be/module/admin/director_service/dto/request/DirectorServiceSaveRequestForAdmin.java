package com.motd.be.module.admin.director_service.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;

import com.motd.be.module.member.director_service.entity.DirectorService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class DirectorServiceSaveRequestForAdmin {

	@NotBlank(message = DIRECTOR_SERVICE_NAME_REQUIRED)
	private String name;
	private Long parentId;
	@NotNull(message = DIRECTOR_SERVICE_IS_ACTIVE_REQUIRED)
	private Boolean isActive;
	@NotNull(message = DIRECTOR_SERVICE_SORT_ORDER_REQUIRED)
	private Integer sortOrder;

	public DirectorService toEntity(DirectorService parent) {
		return DirectorService.builder()
			.name(this.name)
			.parent(parent)
			.isActive(this.isActive)
			.isDeleted(false)
			.sortOrder(this.sortOrder)
			.build();
	}
}
