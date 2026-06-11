package com.motd.be.module.member.consulting_sheet_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.FileException;
import com.motd.be.module.member.consulting_sheet_file.entity.ConsultingSheetFile;
import com.motd.be.module.member.consulting_sheet_file.repository.ConsultingSheetFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultingSheetFileQueryService {

	private final ConsultingSheetFileRepository consultingSheetFileRepository;

	public List<ConsultingSheetFile> findAllByIds(List<Long> ids) {
		return consultingSheetFileRepository.findAllByIds(ids);
	}

	public ConsultingSheetFile findByFileKey(String fileKey) {
		return consultingSheetFileRepository.findByFileKey(fileKey)
			.orElseThrow(() -> new CustomRuntimeException(FileException.FILE_NOT_FOUND));
	}
}
