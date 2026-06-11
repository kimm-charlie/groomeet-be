package com.motd.be.module.member.consulting_request_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.FileException;
import com.motd.be.module.member.consulting_request_file.entity.ConsultingRequestFile;
import com.motd.be.module.member.consulting_request_file.repository.ConsultingRequestFileRepository;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultingRequestFileQueryService {

	private final ConsultingRequestFileRepository consultingRequestFileRepository;

	public List<ConsultingRequestFile> findAllByIds(List<Long> ids) {
		return consultingRequestFileRepository.findAllByIds(ids);
	}

	public List<ConsultingRequestFile> findAllByIdsAndMember(List<Long> ids, Member member) {
		return consultingRequestFileRepository.findAllByIdsAndMember(ids, member);
	}

	public ConsultingRequestFile findByFileKey(String fileKey) {
		return consultingRequestFileRepository.findByFileKey(fileKey)
			.orElseThrow(() -> new CustomRuntimeException(FileException.FILE_NOT_FOUND));
	}
}
