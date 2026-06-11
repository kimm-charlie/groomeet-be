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
public class ServiceRequestSaveDirectRequest {

	@NotNull(message = DIRECTOR_SERVICE_MUST_BE_SELECTED)
	private Long directorServiceId;
	@NotNull(message = DIRECT_REQUEST_MEMBER_ID_REQUIRED)
	private Long directRequestedMemberId;
	@NotEmpty(message = WISH_TIME_REQUIRED)
	@Size(max = MAX_WISH_TIME_COUNT, message = WISH_TIME_EXCEED_MAX_COUNT)
	private List<@NotBlank(message = WISH_TIME_INVALID_FORMAT) @Pattern(regexp = "^\\d{4}\\.\\d{2}\\.\\d{2} ([01]\\d|2[0-3]):[0-5]\\d$", message = WISH_TIME_INVALID_FORMAT) String> wishTimes;
	@Size(max = MAX_ADDITIONAL_REQUEST_LENGTH, message = ADDITIONAL_REQUEST_EXCEED_MAX_LENGTH)
	private String additionalRequest;

	public ServiceRequest toEntity(Member member, DirectorService directorService) {
		return ServiceRequest.builder()
			.member(member)
			.directorService(directorService)
			.expiredAt(LocalDateTime.now().plusDays(SERVICE_REQUEST_EXPIRE_DAYS))
			.status(ServiceRequestStatus.PENDING)
			.additionalRequest(additionalRequest)
			.build();
	}
}
