package com.motd.be.module.member.consulting_sheet.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ConsultingSheetException;
import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.consulting_sheet.enums.ConsultingSheetStatus;
import com.motd.be.module.member.consulting_sheet.repository.ConsultingSheetRepository;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultingSheetQueryService {

	private final ConsultingSheetRepository consultingSheetRepository;

	public ConsultingSheet findApprovedByMember(Member member) {
		return consultingSheetRepository.findByConsultingRequestMemberAndStatus(member, ConsultingSheetStatus.APPROVED)
			.orElse(null);
	}

	public ConsultingSheet findApprovedByIdWithDetails(Long id) {
		return consultingSheetRepository.findByIdAndStatusWithDetails(id, ConsultingSheetStatus.APPROVED)
			.orElseThrow(() -> new CustomRuntimeException(ConsultingSheetException.NOT_FOUND));
	}
}
