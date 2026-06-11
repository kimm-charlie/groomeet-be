package com.motd.be.module.member.consulting_sheet_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.consulting_sheet_file.entity.ConsultingSheetFile;
import com.motd.be.module.member.consulting_sheet_file.repository.ConsultingSheetFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultingSheetFileCommandService {

	private final ConsultingSheetFileRepository consultingSheetFileRepository;

	public ConsultingSheetFile save(ConsultingSheetFile consultingSheetFile) {
		return consultingSheetFileRepository.save(consultingSheetFile);
	}

	public void mapConsultingSheet(List<ConsultingSheetFile> consultingSheetFiles, ConsultingSheet consultingSheet) {
		consultingSheetFileRepository.mapConsultingSheet(consultingSheetFiles, consultingSheet);
	}
}
