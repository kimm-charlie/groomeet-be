package com.motd.be.module.admin.consulting_request.service;

import static com.motd.be.common.constants.PageSizeConstants.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.module.admin.consulting_request.dto.response.ConsultingRequestFindAllResponseForAdmin;
import com.motd.be.module.admin.consulting_request.dto.response.ConsultingRequestFindDetailResponseForAdmin;
import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultingRequestServiceForAdmin {

	private final ConsultingRequestQueryServiceForAdmin consultingRequestQueryServiceForAdmin;

	public ConsultingRequestFindAllResponseForAdmin findAll(String search, Boolean showAll, int page) {
		Pageable pageable = PageRequest.of(page, CONSULTING_REQUEST_PAGE_SIZE);
		Slice<ConsultingRequest> consultingRequests = consultingRequestQueryServiceForAdmin.findAll(search, showAll,
			pageable);
		Long totalCount = consultingRequestQueryServiceForAdmin.count(search, showAll);
		return ConsultingRequestFindAllResponseForAdmin.of(consultingRequests, totalCount);
	}

	public ConsultingRequestFindDetailResponseForAdmin findDetail(Long consultingRequestId) {
		ConsultingRequest consultingRequest = consultingRequestQueryServiceForAdmin.findById(consultingRequestId);
		return ConsultingRequestFindDetailResponseForAdmin.from(consultingRequest);
	}
}
