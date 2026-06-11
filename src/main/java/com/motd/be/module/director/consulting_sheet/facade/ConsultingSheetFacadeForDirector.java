package com.motd.be.module.director.consulting_sheet.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.consulting_request.service.ConsultingRequestQueryServiceForDirector;
import com.motd.be.module.director.consulting_sheet.dto.request.ConsultingSheetSaveRequestForDirector;
import com.motd.be.module.director.consulting_sheet.service.ConsultingSheetCommandServiceForDirector;
import com.motd.be.module.director.consulting_sheet.service.ConsultingSheetFileQueryServiceForDirector;
import com.motd.be.module.director.consulting_sheet.validator.ConsultingSheetValidatorForDirector;
import com.motd.be.module.director.member.service.MemberQueryServiceForDirector;
import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.consulting_sheet_file.entity.ConsultingSheetFile;
import com.motd.be.module.member.consulting_sheet_file.service.ConsultingSheetFileCommandService;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.shared.forbidden_word.validator.ForbiddenWordValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultingSheetFacadeForDirector {

	private final MemberQueryServiceForDirector memberQueryServiceForDirector;
	private final ConsultingRequestQueryServiceForDirector consultingRequestQueryServiceForDirector;
	private final ConsultingSheetCommandServiceForDirector consultingSheetCommandServiceForDirector;
	private final ConsultingSheetFileQueryServiceForDirector consultingSheetFileQueryServiceForDirector;
	private final ConsultingSheetFileCommandService consultingSheetFileCommandService;
	private final ConsultingSheetValidatorForDirector consultingSheetValidatorForDirector;
	private final ForbiddenWordValidator forbiddenWordValidator;

	@Transactional
	public void save(Long memberId, ConsultingSheetSaveRequestForDirector request) {
		Member director = memberQueryServiceForDirector.findByIdWithDirector(memberId);
		DirectorInfo directorInfo = director.getDirectorInfo();

		ConsultingRequest consultingRequest = consultingRequestQueryServiceForDirector.findByIdWithLock(
			request.getConsultingRequestId());

		consultingSheetValidatorForDirector.validateCanSend(consultingRequest, directorInfo);

		forbiddenWordValidator.validate(request.getContent());

		ConsultingSheet consultingSheet = request.toEntity(directorInfo, consultingRequest);
		ConsultingSheet savedSheet = consultingSheetCommandServiceForDirector.save(consultingSheet);

		consultingRequest.complete();

		if (request.getFileIds() != null && !request.getFileIds().isEmpty()) {
			List<ConsultingSheetFile> files = consultingSheetFileQueryServiceForDirector.findAllByIds(
				request.getFileIds());
			consultingSheetValidatorForDirector.validateFiles(files, request.getFileIds(), memberId);
			consultingSheetFileCommandService.mapConsultingSheet(files, savedSheet);
		}
	}
}
