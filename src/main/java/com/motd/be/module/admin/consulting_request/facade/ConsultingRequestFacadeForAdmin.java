package com.motd.be.module.admin.consulting_request.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.admin.consulting_request.dto.response.ConsultingRequestFindAllResponseForAdmin;
import com.motd.be.module.admin.consulting_request.dto.response.ConsultingRequestFindDetailResponseForAdmin;
import com.motd.be.module.admin.consulting_request.service.ConsultingRequestServiceForAdmin;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultingRequestFacadeForAdmin {

	private final ConsultingRequestServiceForAdmin consultingRequestServiceForAdmin;

	public ConsultingRequestFindAllResponseForAdmin findAll(String search, Boolean showAll, int page) {
		return consultingRequestServiceForAdmin.findAll(search, showAll, page);
	}

	public ConsultingRequestFindDetailResponseForAdmin findDetail(Long consultingRequestId) {
		return consultingRequestServiceForAdmin.findDetail(consultingRequestId);
	}
}
