package com.motd.be.module.director.consulting_request.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.consulting_request.repository.ConsultingRequestRepositoryForDirector;
import com.motd.be.module.member.consulting_request.enums.ConsultingRequestStatus;
import com.motd.be.module.member.director_info.entity.DirectorInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultingRequestCommandServiceForDirector {

	private final ConsultingRequestRepositoryForDirector consultingRequestRepositoryForDirector;

	public void releaseOtherReservations(DirectorInfo directorInfo, Long consultingRequestId) {
		consultingRequestRepositoryForDirector.releaseOtherReservations(
			directorInfo,
			ConsultingRequestStatus.RESERVED,
			ConsultingRequestStatus.PENDING,
			consultingRequestId
		);
	}
}
