package com.motd.be.module.admin.consulting_request.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ConsultingRequestException;
import com.motd.be.module.admin.consulting_request.repository.ConsultingRequestQueryDslRepositoryForAdmin;
import com.motd.be.module.admin.consulting_request.repository.ConsultingRequestRepositoryForAdmin;
import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultingRequestQueryServiceForAdmin {

	private final ConsultingRequestRepositoryForAdmin consultingRequestRepositoryForAdmin;
	private final ConsultingRequestQueryDslRepositoryForAdmin consultingRequestQueryDslRepositoryForAdmin;

	public Slice<ConsultingRequest> findAll(String search, Boolean showAll, Pageable pageable) {
		return consultingRequestQueryDslRepositoryForAdmin.findAll(search, showAll, pageable);
	}

	public Long count(String search, Boolean showAll) {
		return consultingRequestQueryDslRepositoryForAdmin.count(search, showAll);
	}

	public ConsultingRequest findById(Long id) {
		return consultingRequestRepositoryForAdmin.findByIdWithFetch(id)
			.orElseThrow(() -> new CustomRuntimeException(ConsultingRequestException.NOT_FOUND));
	}
}
