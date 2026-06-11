package com.motd.be.module.director.consulting_request.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_request.validator.ConsultingRequestValidator;
import com.motd.be.module.member.director_info.entity.DirectorInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultingRequestServiceForDirector {

	private final ConsultingRequestQueryServiceForDirector consultingRequestQueryServiceForDirector;
	private final ConsultingRequestCommandServiceForDirector consultingRequestCommandServiceForDirector;
	private final ConsultingRequestValidator consultingRequestValidator;

	public ConsultingRequest reserve(DirectorInfo directorInfo, Long consultingRequestId) {
		ConsultingRequest consultingRequest = consultingRequestQueryServiceForDirector.findByIdWithLock(
			consultingRequestId);
		LocalDateTime now = LocalDateTime.now();

		consultingRequestValidator.validateCanReserve(consultingRequest, directorInfo, now);
		consultingRequest.reserve(directorInfo, now);
		consultingRequestCommandServiceForDirector.releaseOtherReservations(directorInfo, consultingRequestId);
		return consultingRequest;
	}
}
