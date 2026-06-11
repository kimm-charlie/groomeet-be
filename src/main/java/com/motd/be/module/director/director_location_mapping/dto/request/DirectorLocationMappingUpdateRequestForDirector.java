package com.motd.be.module.director.director_location_mapping.dto.request;

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
public class DirectorLocationMappingUpdateRequestForDirector {

	@NotEmpty(message = LOCATION_MUST_BE_SELECTED)
	@Size(max = 3, message = LOCATION_EXCEED_MAX_COUNT)
	private List<Long> locationIds;
}
