package com.motd.be.module.admin.service_estimate.facade;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.admin.service_estimate.dto.response.ServiceEstimateFindAllResponseForAdmin;
import com.motd.be.module.admin.service_estimate.dto.response.ServiceEstimateFindDetailResponseForAdmin;
import com.motd.be.module.admin.service_estimate.service.ServiceEstimateServiceForAdmin;
import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.chat_room_service_estimate_mapping.service.ChatRoomServiceEstimateMappingQueryService;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceEstimateFacadeForAdmin {

	private final ServiceEstimateServiceForAdmin serviceEstimateServiceForAdmin;
	private final ChatRoomServiceEstimateMappingQueryService chatRoomServiceEstimateMappingQueryService;

	public ServiceEstimateFindAllResponseForAdmin findAll(String search, ServiceEstimateStatus status, int page) {
		return serviceEstimateServiceForAdmin.findAll(search, status, page);
	}

	public ServiceEstimateFindDetailResponseForAdmin findDetail(Long serviceEstimateId) {
		ServiceEstimate serviceEstimate = serviceEstimateServiceForAdmin.findServiceEstimate(serviceEstimateId);
		Map<Long, ChatRoomServiceEstimateMapping> mappings = chatRoomServiceEstimateMappingQueryService.findAllByServiceEstimates(
			List.of(serviceEstimate));
		return serviceEstimateServiceForAdmin.findDetail(serviceEstimate, mappings);
	}
}
