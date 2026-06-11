package com.motd.be.module.admin.director_service.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;

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
public class DirectorServiceUpdateRequestForAdmin {

	@NotBlank(message = DIRECTOR_SERVICE_NAME_REQUIRED)
	private String name;
	@NotNull(message = DIRECTOR_SERVICE_IS_ACTIVE_REQUIRED)
	private Boolean isActive;
	@NotNull(message = DIRECTOR_SERVICE_SORT_ORDER_REQUIRED)
	private Integer sortOrder;
	private Long parentId;
}
