package com.motd.be.module.director.director_service_mapping.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class DirectorServiceMappingUpdateServiceRequestForDirector {

	@NotEmpty(message = DIRECTOR_SERVICE_MUST_BE_SELECTED)
	@Size(max = 7, message = DIRECTOR_SERVICE_SELECTION_OUT_OF_BOUNDS)
	private List<Long> serviceIds;
}
