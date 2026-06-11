package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.consulting_sheet_file.entity.ConsultingSheetFile;
import com.motd.be.module.member.consulting_sheet_file.repository.ConsultingSheetFileRepository;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.shared.aws.enums.UploadFileType;

@Component
public class ConsultingSheetFileProvider {

	@Autowired
	private ConsultingSheetFileRepository consultingSheetFileRepository;

	public ConsultingSheetFile save(ConsultingSheet consultingSheet, Member member, int sortOrder) {
		return consultingSheetFileRepository.save(ConsultingSheetFile.builder()
			.consultingSheet(consultingSheet)
			.member(member)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.fileKey(FILE_KEY_STR)
			.sortOrder(sortOrder)
			.fileType(UploadFileType.IMAGE)
			.build());
	}

	public ConsultingSheetFile saveWithoutConsultingSheet(Member member, int sortOrder) {
		return consultingSheetFileRepository.save(ConsultingSheetFile.builder()
			.member(member)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.fileKey(FILE_KEY_STR + "_" + sortOrder)
			.sortOrder(sortOrder)
			.fileType(UploadFileType.IMAGE)
			.build());
	}
}
