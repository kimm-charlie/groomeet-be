package com.motd.be.module.member.consulting_request.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.consulting_request.repository.ConsultingRequestRepository;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultingRequestQueryService {

	private final ConsultingRequestRepository consultingRequestRepository;

	public boolean existsByMember(Member member) {
		return consultingRequestRepository.existsByMember(member);
	}
}
