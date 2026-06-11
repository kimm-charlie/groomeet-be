package com.motd.be.module.director.consulting_request.service;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ConsultingRequestException;
import com.motd.be.module.director.consulting_request.repository.ConsultingRequestQueryDslRepositoryForDirector;
import com.motd.be.module.director.consulting_request.repository.ConsultingRequestRepositoryForDirector;
import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultingRequestQueryServiceForDirector {

	private final ConsultingRequestRepositoryForDirector consultingRequestRepositoryForDirector;
	private final ConsultingRequestQueryDslRepositoryForDirector consultingRequestQueryDslRepositoryForDirector;

	public Slice<ConsultingRequest> findAllAvailable(Long cursorId) {
		return consultingRequestQueryDslRepositoryForDirector.findAllAvailable(cursorId);
	}

	public long countAvailable() {
		return consultingRequestQueryDslRepositoryForDirector.countAvailable();
	}

	public ConsultingRequest findById(Long consultingRequestId) {
		return consultingRequestRepositoryForDirector.findByIdAndNotDeleted(consultingRequestId)
			.orElseThrow(() -> new CustomRuntimeException(ConsultingRequestException.NOT_FOUND));
	}

	public ConsultingRequest findByIdWithLock(Long consultingRequestId) {
		return consultingRequestRepositoryForDirector.findByIdWithLock(consultingRequestId)
			.orElseThrow(() -> new CustomRuntimeException(ConsultingRequestException.NOT_FOUND));
	}
}
