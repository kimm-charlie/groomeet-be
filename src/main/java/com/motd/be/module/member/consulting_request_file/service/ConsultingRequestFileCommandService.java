package com.motd.be.module.member.consulting_request_file.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_request_file.entity.ConsultingRequestFile;
import com.motd.be.module.member.consulting_request_file.enums.ConsultingRequestImageCategory;
import com.motd.be.module.member.consulting_request_file.repository.ConsultingRequestFileRepository;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultingRequestFileCommandService {

	private final ConsultingRequestFileRepository consultingRequestFileRepository;

	public ConsultingRequestFile save(ConsultingRequestFile consultingRequestFile) {
		return consultingRequestFileRepository.save(consultingRequestFile);
	}

	public int updateConsultingRequestMapping(Long fileId, Member member, ConsultingRequest consultingRequest,
		ConsultingRequestImageCategory imageCategory, int sortOrder) {
		return consultingRequestFileRepository.updateConsultingRequestMapping(fileId, member, consultingRequest,
			imageCategory, sortOrder);
	}
}
