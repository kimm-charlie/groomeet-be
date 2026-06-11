package com.motd.be.module.admin.consulting_sheet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ConsultingSheetException;
import com.motd.be.module.admin.consulting_sheet.repository.ConsultingSheetRepositoryForAdmin;
import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultingSheetQueryServiceForAdmin {

	private final ConsultingSheetRepositoryForAdmin consultingSheetRepositoryForAdmin;

	public ConsultingSheet findByIdWithLock(Long id) {
		return consultingSheetRepositoryForAdmin.findByIdWithLock(id)
			.orElseThrow(() -> new CustomRuntimeException(ConsultingSheetException.NOT_FOUND));
	}
}
