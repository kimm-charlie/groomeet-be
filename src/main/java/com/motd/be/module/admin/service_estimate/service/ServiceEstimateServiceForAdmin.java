package com.motd.be.module.admin.service_estimate.service;

import static com.motd.be.common.constants.PageSizeConstants.*;

import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.module.admin.service_estimate.dto.response.ServiceEstimateFindAllResponseForAdmin;
import com.motd.be.module.admin.service_estimate.dto.response.ServiceEstimateFindDetailResponseForAdmin;
import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.review.entity.Review;
import com.motd.be.module.member.review.repository.ReviewRepository;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceEstimateServiceForAdmin {

	private final ServiceEstimateQueryServiceForAdmin serviceEstimateQueryServiceForAdmin;
	private final ReviewRepository reviewRepository;

	public ServiceEstimateFindAllResponseForAdmin findAll(String search, ServiceEstimateStatus status, int page) {
		Pageable pageable = PageRequest.of(page, SERVICE_ESTIMATE_FIND_ALL_SIZE);

		Slice<ServiceEstimate> serviceEstimates = serviceEstimateQueryServiceForAdmin.findAll(search, status,
			pageable);
		return ServiceEstimateFindAllResponseForAdmin.from(serviceEstimates);
	}

	public ServiceEstimate findServiceEstimate(Long serviceEstimateId) {
		return serviceEstimateQueryServiceForAdmin.findById(serviceEstimateId);
	}

	public ServiceEstimateFindDetailResponseForAdmin findDetail(ServiceEstimate serviceEstimate,
		Map<Long, ChatRoomServiceEstimateMapping> mappings) {
		Review review = reviewRepository.findByServiceEstimateId(serviceEstimate.getId())
			.orElse(null);
		return ServiceEstimateFindDetailResponseForAdmin.of(serviceEstimate, review, mappings);
	}
}
