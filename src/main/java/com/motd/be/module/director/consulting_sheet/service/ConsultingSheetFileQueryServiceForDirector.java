package com.motd.be.module.director.consulting_sheet.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.consulting_sheet_file.entity.ConsultingSheetFile;
import com.motd.be.module.member.consulting_sheet_file.repository.ConsultingSheetFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultingSheetFileQueryServiceForDirector {

	private final ConsultingSheetFileRepository consultingSheetFileRepository;

	public List<ConsultingSheetFile> findAllByIds(List<Long> ids) {
		return consultingSheetFileRepository.findAllByIds(ids);
	}
}
