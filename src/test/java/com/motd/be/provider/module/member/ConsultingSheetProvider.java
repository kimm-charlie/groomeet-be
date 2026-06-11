package com.motd.be.provider.module.member;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.consulting_sheet.enums.ConsultingSheetStatus;
import com.motd.be.module.member.consulting_sheet.repository.ConsultingSheetRepository;
import com.motd.be.module.member.director_info.entity.DirectorInfo;

@Component
public class ConsultingSheetProvider {

	@Autowired
	private ConsultingSheetRepository consultingSheetRepository;

	public ConsultingSheet saveApproved(ConsultingRequest consultingRequest, DirectorInfo directorInfo) {
		return consultingSheetRepository.save(ConsultingSheet.builder()
			.consultingRequest(consultingRequest)
			.directorInfo(directorInfo)
			.content("컨설팅 내용")
			.price("50000")
			.status(ConsultingSheetStatus.APPROVED)
			.approvedAt(LocalDateTime.now())
			.build());
	}

	public ConsultingSheet savePendingApproval(ConsultingRequest consultingRequest, DirectorInfo directorInfo) {
		return consultingSheetRepository.save(ConsultingSheet.builder()
			.consultingRequest(consultingRequest)
			.directorInfo(directorInfo)
			.content("컨설팅 내용")
			.price("50000")
			.status(ConsultingSheetStatus.PENDING_APPROVAL)
			.build());
	}

	public ConsultingSheet saveRejected(ConsultingRequest consultingRequest, DirectorInfo directorInfo) {
		return consultingSheetRepository.save(ConsultingSheet.builder()
			.consultingRequest(consultingRequest)
			.directorInfo(directorInfo)
			.content("컨설팅 내용")
			.price("50000")
			.status(ConsultingSheetStatus.REJECTED)
			.build());
	}
}
