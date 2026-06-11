package com.motd.be.module.member.consulting_sheet.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.consulting_sheet.repository.ConsultingSheetRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultingSheetCommandService {

	private final ConsultingSheetRepository consultingSheetRepository;

	public ConsultingSheet save(ConsultingSheet consultingSheet) {
		return consultingSheetRepository.save(consultingSheet);
	}
}
