package com.motd.be.module.member.consulting_request.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_request.repository.ConsultingRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultingRequestCommandService {

	private final ConsultingRequestRepository consultingRequestRepository;

	public ConsultingRequest save(ConsultingRequest consultingRequest) {
		return consultingRequestRepository.save(consultingRequest);
	}
}
