package com.motd.be.module.director.director_service_mapping.dto.response;

import static com.motd.be.common.constants.Constants.*;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DirectorServiceFindActivationProgressResponseForDirector {

	private Integer progressPercentage;

	public static DirectorServiceFindActivationProgressResponseForDirector from(int maxActivationCount) {
		return DirectorServiceFindActivationProgressResponseForDirector.builder()
			.progressPercentage(
				maxActivationCount * 100 / ACTIVATION_REQUIRED_MINIMUM_DIRECTOR_COUNT
			)
			.build();
	}
}
