package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;
import static com.motd.be.common.utils.Utils.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateReminderStatus;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_estimate.repository.ServiceEstimateRepository;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

@Component
public class ServiceEstimateProvider {

	@Autowired
	private ServiceEstimateRepository serviceEstimateRepository;

	public List<ServiceEstimate> findAll() {
		return serviceEstimateRepository.findAll();
	}

	public ServiceEstimate save(DirectorInfo directorInfo, ServiceRequest serviceRequest) {
		return serviceEstimateRepository.save(
			ServiceEstimate.builder()
				.directorInfo(directorInfo)
				.serviceRequest(serviceRequest)
				.status(ServiceEstimateStatus.PENDING)
				.title(TITLE_STR)
				.price(10000L)
				.content(CONTENT_STR)
				.activeUniqueKey(generateServiceEstimateUniqueKey(directorInfo, serviceRequest))
				.build()
		);
	}

	public ServiceEstimate saveWithScheduledAt(DirectorInfo directorInfo, ServiceRequest serviceRequest,
		LocalDateTime scheduledAt) {
		return serviceEstimateRepository.save(
			ServiceEstimate.builder()
				.directorInfo(directorInfo)
				.serviceRequest(serviceRequest)
				.status(ServiceEstimateStatus.PENDING)
				.title(TITLE_STR)
				.price(10000L)
				.content(CONTENT_STR)
				.scheduledAt(scheduledAt)
				.activeUniqueKey(generateServiceEstimateUniqueKey(directorInfo, serviceRequest))
				.build()
		);
	}

	public ServiceEstimate saveWithIsDeletedTrue(DirectorInfo directorInfo,
		ServiceRequest serviceRequestPendingStatus2) {
		return serviceEstimateRepository.save(
			ServiceEstimate.builder()
				.directorInfo(directorInfo)
				.serviceRequest(serviceRequestPendingStatus2)
				.status(ServiceEstimateStatus.CANCELED)
				.title(TITLE_STR)
				.price(10000L)
				.content(CONTENT_STR)
				.activeUniqueKey(generateServiceEstimateUniqueKey(directorInfo, serviceRequestPendingStatus2))
				.isDeleted(Boolean.TRUE)
				.build()
		);
	}

	public ServiceEstimate saveOngoing(DirectorInfo directorInfo, ServiceRequest serviceRequest,
		LocalDateTime ongoingAt) {
		return serviceEstimateRepository.save(
			ServiceEstimate.builder()
				.directorInfo(directorInfo)
				.serviceRequest(serviceRequest)
				.title(TITLE_STR)
				.price(10000L)
				.content(CONTENT_STR)
				.status(ServiceEstimateStatus.ONGOING)
				.ongoingAt(ongoingAt)
				.activeUniqueKey(generateServiceEstimateUniqueKey(directorInfo, serviceRequest))
				.isHired(Boolean.TRUE)
				.build()
		);
	}

	public ServiceEstimate saveOngoing(DirectorInfo directorInfo, ServiceRequest serviceRequest,
		LocalDateTime ongoingAt, LocalDateTime scheduledAt) {
		return serviceEstimateRepository.save(
			ServiceEstimate.builder()
				.directorInfo(directorInfo)
				.serviceRequest(serviceRequest)
				.title(TITLE_STR)
				.price(10000L)
				.content(CONTENT_STR)
				.status(ServiceEstimateStatus.ONGOING)
				.ongoingAt(ongoingAt)
				.scheduledAt(scheduledAt)
				.activeUniqueKey(generateServiceEstimateUniqueKey(directorInfo, serviceRequest))
				.isHired(Boolean.TRUE)
					.build()
		);
	}

	public ServiceEstimate saveOngoingWithPendingReminder(DirectorInfo directorInfo, ServiceRequest serviceRequest,
		LocalDateTime ongoingAt, LocalDateTime scheduledAt, LocalDateTime reminderNeedAt) {
		return serviceEstimateRepository.save(
			ServiceEstimate.builder()
				.directorInfo(directorInfo)
				.serviceRequest(serviceRequest)
				.title(TITLE_STR)
				.price(10000L)
				.content(CONTENT_STR)
				.status(ServiceEstimateStatus.ONGOING)
				.ongoingAt(ongoingAt)
				.scheduledAt(scheduledAt)
				.reminderNeedAt(reminderNeedAt)
				.reminderStatus(ServiceEstimateReminderStatus.PENDING)
				.activeUniqueKey(generateServiceEstimateUniqueKey(directorInfo, serviceRequest))
				.isHired(Boolean.TRUE)
				.build()
		);
	}

	public ServiceEstimate saveOngoingWithSentReminder(DirectorInfo directorInfo, ServiceRequest serviceRequest,
		LocalDateTime ongoingAt, LocalDateTime scheduledAt, LocalDateTime reminderNeedAt, LocalDateTime reminderSentAt) {
		return serviceEstimateRepository.save(
			ServiceEstimate.builder()
				.directorInfo(directorInfo)
				.serviceRequest(serviceRequest)
				.title(TITLE_STR)
				.price(10000L)
				.content(CONTENT_STR)
				.status(ServiceEstimateStatus.ONGOING)
				.ongoingAt(ongoingAt)
				.scheduledAt(scheduledAt)
				.reminderNeedAt(reminderNeedAt)
				.reminderStatus(ServiceEstimateReminderStatus.SENT)
				.reminderSentAt(reminderSentAt)
				.activeUniqueKey(generateServiceEstimateUniqueKey(directorInfo, serviceRequest))
				.isHired(Boolean.TRUE)
				.build()
		);
	}

	public ServiceEstimate saveOngoingWithIsDeletedTrue(DirectorInfo directorInfo, ServiceRequest serviceRequest,
		LocalDateTime ongoingAt) {
		return serviceEstimateRepository.save(
			ServiceEstimate.builder()
				.directorInfo(directorInfo)
				.serviceRequest(serviceRequest)
				.title(TITLE_STR)
				.price(10000L)
				.content(CONTENT_STR)
				.status(ServiceEstimateStatus.ONGOING)
				.ongoingAt(ongoingAt)
				.isDeleted(Boolean.TRUE)
				.activeUniqueKey(generateServiceEstimateUniqueKey(directorInfo, serviceRequest))
				.build()
		);
	}

	public ServiceEstimate saveDirectorDone(DirectorInfo directorInfo, ServiceRequest serviceRequest,
		LocalDateTime completedAt) {
		return serviceEstimateRepository.save(
			ServiceEstimate.builder()
				.directorInfo(directorInfo)
				.serviceRequest(serviceRequest)
				.title(TITLE_STR)
				.price(10000L)
				.content(CONTENT_STR)
				.status(ServiceEstimateStatus.DIRECTOR_DONE)
				.ongoingAt(completedAt)
				.directorDoneAt(completedAt)
				.activeUniqueKey(generateServiceEstimateUniqueKey(directorInfo, serviceRequest))
				.build()
		);
	}

	public ServiceEstimate saveDirectorDoneWithHired(DirectorInfo directorInfo, ServiceRequest serviceRequest,
		LocalDateTime completedAt) {
		return serviceEstimateRepository.save(
			ServiceEstimate.builder()
				.directorInfo(directorInfo)
				.serviceRequest(serviceRequest)
				.title(TITLE_STR)
				.price(10000L)
				.content(CONTENT_STR)
				.status(ServiceEstimateStatus.DIRECTOR_DONE)
				.ongoingAt(completedAt)
				.directorDoneAt(completedAt)
				.isHired(Boolean.TRUE)
				.activeUniqueKey(generateServiceEstimateUniqueKey(directorInfo, serviceRequest))
				.build()
		);
	}

	public ServiceEstimate saveDirectorDoneAndIsDeletedTrue(DirectorInfo directorInfo,
		ServiceRequest serviceRequest,
		LocalDateTime completedAt) {
		return serviceEstimateRepository.save(
			ServiceEstimate.builder()
				.directorInfo(directorInfo)
				.serviceRequest(serviceRequest)
				.title(TITLE_STR)
				.price(10000L)
				.content(CONTENT_STR)
				.status(ServiceEstimateStatus.DIRECTOR_DONE)
				.ongoingAt(completedAt)
				.directorDoneAt(completedAt)
				.activeUniqueKey(generateServiceEstimateUniqueKey(directorInfo, serviceRequest))
				.isDeleted(Boolean.TRUE)
				.build()
		);
	}

	public ServiceEstimate saveReviewCompleted(DirectorInfo directorInfo, ServiceRequest serviceRequest,
		LocalDateTime completedAt) {
		return saveReviewCompleted(directorInfo, serviceRequest, completedAt, null);
	}

	public ServiceEstimate saveReviewCompleted(DirectorInfo directorInfo, ServiceRequest serviceRequest,
		LocalDateTime completedAt, LocalDateTime scheduledAt) {
		return serviceEstimateRepository.save(
			ServiceEstimate.builder()
				.directorInfo(directorInfo)
				.serviceRequest(serviceRequest)
				.title(TITLE_STR)
				.price(10000L)
				.content(CONTENT_STR)
				.status(ServiceEstimateStatus.REVIEW_COMPLETED)
				.ongoingAt(completedAt)
				.directorDoneAt(completedAt)
				.memberCompletedAt(completedAt)
				.scheduledAt(scheduledAt)
				.activeUniqueKey(generateServiceEstimateUniqueKey(directorInfo, serviceRequest))
				.build()
		);
	}

	public ServiceEstimate saveMemberCompleted(DirectorInfo directorInfo, ServiceRequest serviceRequest,
		LocalDateTime completedAt) {
		return serviceEstimateRepository.save(
			ServiceEstimate.builder()
				.directorInfo(directorInfo)
				.serviceRequest(serviceRequest)
				.title(TITLE_STR)
				.price(10000L)
				.content(CONTENT_STR)
				.status(ServiceEstimateStatus.COMPLETED_BY_MEMBER)
				.ongoingAt(completedAt)
				.directorDoneAt(completedAt)
				.memberCompletedAt(completedAt)
				.activeUniqueKey(generateServiceEstimateUniqueKey(directorInfo, serviceRequest))
				.build()
		);
	}

	public ServiceEstimate saveMemberCompleted(DirectorInfo directorInfo, ServiceRequest serviceRequest,
		LocalDateTime completedAt, LocalDateTime scheduledAt) {
		return serviceEstimateRepository.save(
			ServiceEstimate.builder()
				.directorInfo(directorInfo)
				.serviceRequest(serviceRequest)
				.title(TITLE_STR)
				.price(10000L)
				.content(CONTENT_STR)
				.status(ServiceEstimateStatus.COMPLETED_BY_MEMBER)
				.ongoingAt(completedAt)
				.directorDoneAt(completedAt)
				.memberCompletedAt(completedAt)
				.scheduledAt(scheduledAt)
				.activeUniqueKey(generateServiceEstimateUniqueKey(directorInfo, serviceRequest))
				.build()
		);
	}

	public ServiceEstimate saveMemberCompletedWithReviewReminderSent(DirectorInfo directorInfo, ServiceRequest serviceRequest,
		LocalDateTime completedAt) {
		return saveMemberCompletedWithReviewReminderSent(directorInfo, serviceRequest, completedAt, null);
	}

	public ServiceEstimate saveMemberCompletedWithReviewReminderSent(DirectorInfo directorInfo, ServiceRequest serviceRequest,
		LocalDateTime completedAt, LocalDateTime scheduledAt) {
		return serviceEstimateRepository.save(
			ServiceEstimate.builder()
				.directorInfo(directorInfo)
				.serviceRequest(serviceRequest)
				.title(TITLE_STR)
				.price(10000L)
				.content(CONTENT_STR)
				.status(ServiceEstimateStatus.COMPLETED_BY_MEMBER)
				.ongoingAt(completedAt)
				.directorDoneAt(completedAt)
				.memberCompletedAt(completedAt)
				.reviewReminderSentAt(completedAt)
				.scheduledAt(scheduledAt)
				.activeUniqueKey(generateServiceEstimateUniqueKey(directorInfo, serviceRequest))
				.build()
		);
	}

	public ServiceEstimate saveCanceled(DirectorInfo directorInfo, ServiceRequest serviceRequest,
		LocalDateTime canceledAt) {
		return serviceEstimateRepository.save(
			ServiceEstimate.builder()
				.directorInfo(directorInfo)
				.serviceRequest(serviceRequest)
				.title(TITLE_STR)
				.price(10000L)
				.content(CONTENT_STR)
				.ongoingAt(canceledAt)
				.status(ServiceEstimateStatus.CANCELED)
				.canceledAt(canceledAt)
				.activeUniqueKey(generateServiceEstimateUniqueKey(directorInfo, serviceRequest))
				.build()
		);
	}

	public ServiceEstimate saveExpired(DirectorInfo directorInfo, ServiceRequest expiredServiceRequest,
		LocalDateTime localDateTime) {
		return serviceEstimateRepository.save(
			ServiceEstimate.builder()
				.directorInfo(directorInfo)
				.serviceRequest(expiredServiceRequest)
				.title(TITLE_STR)
				.price(10000L)
				.content(CONTENT_STR)
				.status(ServiceEstimateStatus.EXPIRED)
				.expiredAt(localDateTime)
				.activeUniqueKey(generateServiceEstimateUniqueKey(directorInfo, expiredServiceRequest))
				.build()
		);
	}

	public ServiceEstimate findById(Long id) {
		return serviceEstimateRepository.findById(id).orElseThrow();
	}
}
