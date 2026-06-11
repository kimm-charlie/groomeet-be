package com.motd.be.module.member.service_estimate.service;

import static com.motd.be.common.constants.Constants.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_estimate.repository.ServiceEstimateRepository;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceEstimateCommandService {

	private final ServiceEstimateRepository serviceEstimateRepository;

	public ServiceEstimate save(ServiceEstimate entity) {
		return serviceEstimateRepository.save(entity);
	}

	public void updateToExpiredStatusByServiceRequests(List<ServiceRequest> serviceRequests) {
		serviceEstimateRepository.updateToExpiredStatusByServiceRequests(serviceRequests, ServiceEstimateStatus.EXPIRED,
			ServiceEstimateStatus.PENDING, LocalDateTime.now());
	}

	public void updateToCanceledStatusByServiceRequests(List<ServiceRequest> serviceRequests) {
		serviceEstimateRepository.updateStatusToCanceledByServiceRequests(serviceRequests,
			ServiceEstimateStatus.CANCELED, LocalDateTime.now());
	}

	public void updateToCanceledStatusByServiceEstimates(List<ServiceEstimate> serviceEstimates) {
		serviceEstimateRepository.updateStatusToCanceledByServiceEstimates(serviceEstimates,
			ServiceEstimateStatus.CANCELED, LocalDateTime.now());
	}

	/**
	 * serviceRequest 에서 파생된 제안중
	 * serviceEstimate(간택당한) 을 제외한 나머지 제안에 대해서 일괄 만료처리하는 로직
	 *
	 * @param serviceEstimate
	 * @param serviceRequest
	 */
	public void updateToExpiredStatusByServiceRequest(ServiceEstimate serviceEstimate, ServiceRequest serviceRequest) {
		serviceEstimateRepository.updateToExpiredStatusByServiceRequest(serviceEstimate, serviceRequest,
			ServiceEstimateStatus.EXPIRED, LocalDateTime.now(), ENDED_ESTIMATE_STATUSES);
	}

	public void updateToDirectorCompleted(List<ServiceEstimate> serviceEstimates) {
		serviceEstimateRepository.updateToDirectorCompleted(serviceEstimates,
			ServiceEstimateStatus.DIRECTOR_DONE, LocalDateTime.now());
	}

	public void updateToMemberCompleted(List<ServiceEstimate> targetEstimates) {
		serviceEstimateRepository.updateToMemberCompleted(targetEstimates,
			ServiceEstimateStatus.COMPLETED_BY_MEMBER, LocalDateTime.now());
	}

	public void updateReviewReminderSentAt(List<ServiceEstimate> serviceEstimates) {
		serviceEstimateRepository.updateReviewReminderSentAt(serviceEstimates, LocalDateTime.now());
	}
}
