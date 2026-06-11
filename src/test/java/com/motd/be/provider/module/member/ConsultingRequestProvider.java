package com.motd.be.provider.module.member;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_request.enums.ConsultingRequestStatus;
import com.motd.be.module.member.consulting_request.repository.ConsultingRequestRepository;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.member.entity.Member;

@Component
public class ConsultingRequestProvider {

	@Autowired
	private ConsultingRequestRepository consultingRequestRepository;

	public ConsultingRequest save(Member member) {
		return consultingRequestRepository.save(ConsultingRequest.builder()
			.member(member)
			.usesHairProduct(true)
			.prefersExposedForehead(false)
			.recentProcedure("없음")
			.status(ConsultingRequestStatus.PENDING)
			.build());
	}

	public ConsultingRequest saveReserved(Member member, DirectorInfo reservedBy, LocalDateTime reservedAt) {
		return consultingRequestRepository.save(ConsultingRequest.builder()
			.member(member)
			.usesHairProduct(true)
			.prefersExposedForehead(false)
			.recentProcedure("없음")
			.status(ConsultingRequestStatus.RESERVED)
			.reservedBy(reservedBy)
			.reservedAt(reservedAt)
			.build());
	}

	public ConsultingRequest saveCompleted(Member member) {
		return consultingRequestRepository.save(ConsultingRequest.builder()
			.member(member)
			.usesHairProduct(true)
			.prefersExposedForehead(false)
			.recentProcedure("없음")
			.status(ConsultingRequestStatus.COMPLETED)
			.build());
	}

	public List<ConsultingRequest> findAll() {
		return consultingRequestRepository.findAll();
	}
}
