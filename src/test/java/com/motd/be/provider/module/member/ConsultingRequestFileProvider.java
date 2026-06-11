package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_request_file.entity.ConsultingRequestFile;
import com.motd.be.module.member.consulting_request_file.enums.ConsultingRequestImageCategory;
import com.motd.be.module.member.consulting_request_file.repository.ConsultingRequestFileRepository;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.shared.aws.enums.UploadFileType;

@Component
public class ConsultingRequestFileProvider {

	@Autowired
	private ConsultingRequestFileRepository consultingRequestFileRepository;

	public ConsultingRequestFile save(ConsultingRequest consultingRequest, Member member,
		ConsultingRequestImageCategory imageCategory, int sortOrder) {
		return consultingRequestFileRepository.save(ConsultingRequestFile.builder()
			.consultingRequest(consultingRequest)
			.member(member)
			.imageCategory(imageCategory)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.fileKey(FILE_KEY_STR)
			.sortOrder(sortOrder)
			.fileType(UploadFileType.IMAGE)
			.build());
	}

	public ConsultingRequestFile saveWithoutConsultingRequest(Member member) {
		return consultingRequestFileRepository.save(ConsultingRequestFile.builder()
			.member(member)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.fileKey(FILE_KEY_STR)
			.sortOrder(0)
			.fileType(UploadFileType.IMAGE)
			.build());
	}

	public List<ConsultingRequestFile> findAll() {
		return consultingRequestFileRepository.findAll();
	}
}
