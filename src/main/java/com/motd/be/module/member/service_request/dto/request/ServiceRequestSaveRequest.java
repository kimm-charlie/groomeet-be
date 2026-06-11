package com.motd.be.module.member.service_request.dto.request;

import static com.motd.be.common.constants.TimePolicy.*;
import static com.motd.be.common.constants.ValidationConstants.*;
import static com.motd.be.common.constants.ValidationMessages.*;
import java.time.LocalDateTime;
import java.util.List;

import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class ServiceRequestSaveRequest {

	@NotEmpty(message = LOCATION_MUST_BE_SELECTED)
	@Size(max = MAX_LOCATION_COUNT_IN_SERVICE_REQUEST, message = LOCATION_EXCEED_MAX_COUNT)
	private List<Long> locationIds;
	@NotNull(message = DIRECTOR_SERVICE_MUST_BE_SELECTED)
	private Long directorServiceId;
	@NotBlank(message = AI_CONTENT_REQUIRED)
	@Size(max = MAX_AI_CONTENT_LENGTH, message = AI_CONTENT_EXCEED_MAX_LENGTH)
	private String aiContent;
	@NotEmpty(message = WISH_TIME_REQUIRED)
	@Size(max = MAX_WISH_TIME_COUNT, message = WISH_TIME_EXCEED_MAX_COUNT)
	private List<@Pattern(regexp = "^\\d{4}\\.\\d{2}\\.\\d{2} ([01]\\d|2[0-3]):[0-5]\\d$", message = WISH_TIME_INVALID_FORMAT) String> wishTimes;
	@Size(max = MAX_FILE_UPLOAD_COUNT_IN_SERVICE_REQUEST, message = FILE_UPLOAD_EXCEED_MAX_COUNT)
	private List<Long> fileIds;

	public ServiceRequest toEntity(Member member, DirectorService directorService) {
		return ServiceRequest.builder()
			.member(member)
			.directorService(directorService)
			.expiredAt(LocalDateTime.now().plusDays(SERVICE_REQUEST_EXPIRE_DAYS))
			.status(ServiceRequestStatus.PENDING)
			.aiContent(aiContent)
			.build();
	}
}
