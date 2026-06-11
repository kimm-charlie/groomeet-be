package com.motd.be.module.director.consulting_request.facade;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.consulting_request.dto.response.ConsultingRequestFindAllResponseForDirector;
import com.motd.be.module.director.consulting_request.dto.response.ConsultingRequestResponseForDirector;
import com.motd.be.module.director.consulting_request.service.ConsultingRequestServiceForDirector;
import com.motd.be.module.director.member.service.MemberQueryServiceForDirector;
import com.motd.be.module.director.consulting_request.service.ConsultingRequestQueryServiceForDirector;
import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_request.validator.ConsultingRequestValidator;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultingRequestFacadeForDirector {

	private final MemberQueryServiceForDirector memberQueryServiceForDirector;
	private final ConsultingRequestQueryServiceForDirector consultingRequestQueryServiceForDirector;
	private final ConsultingRequestServiceForDirector consultingRequestServiceForDirector;
	private final ConsultingRequestValidator consultingRequestValidator;

	public ConsultingRequestFindAllResponseForDirector findAll(Long cursorId) {
		Slice<ConsultingRequest> consultingRequests = consultingRequestQueryServiceForDirector.findAllAvailable(
			cursorId);
		long totalCount = consultingRequestQueryServiceForDirector.countAvailable();

		return ConsultingRequestFindAllResponseForDirector.of(consultingRequests, totalCount);
	}

	@Transactional
	public void cancelReservation(Long memberId, Long consultingRequestId) {
		Member director = memberQueryServiceForDirector.findByIdWithDirector(memberId);
		ConsultingRequest consultingRequest = consultingRequestQueryServiceForDirector.findById(
			consultingRequestId);

		if (consultingRequestValidator.canCancelReservation(consultingRequest, director.getDirectorInfo())) {
			consultingRequest.cancelReservation();
		}
	}

	@Transactional
	public ConsultingRequestResponseForDirector reserve(Long memberId, Long consultingRequestId) {
		Member director = memberQueryServiceForDirector.findByIdWithDirector(memberId);
		ConsultingRequest consultingRequest = consultingRequestServiceForDirector.reserve(director.getDirectorInfo(),
			consultingRequestId);
		return ConsultingRequestResponseForDirector.from(consultingRequest);
	}
}
