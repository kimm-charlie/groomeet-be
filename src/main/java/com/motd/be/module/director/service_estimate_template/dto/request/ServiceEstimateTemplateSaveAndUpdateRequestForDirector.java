package com.motd.be.module.director.service_estimate_template.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;

import java.util.List;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.service_estimate_template.entity.ServiceEstimateTemplate;

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
public class ServiceEstimateTemplateSaveAndUpdateRequestForDirector {

	@NotNull(message = DIRECTOR_SERVICE_MUST_BE_SELECTED)
	private Long serviceId;
	@NotNull(message = PRICE_REQUIRED)
	private Long price;
	@NotBlank(message = TITLE_REQUIRED)
	private String title;
	@NotBlank(message = CONTENT_REQUIRED)
	private String content;
	private List<Long> fileIds;

	public ServiceEstimateTemplate toEntity(DirectorInfo directorInfo, DirectorService directorService) {
		return ServiceEstimateTemplate.builder()
			.directorInfo(directorInfo)
			.directorService(directorService)
			.price(this.price)
			.title(this.title)
			.content(this.content)
			.build();
	}
}
