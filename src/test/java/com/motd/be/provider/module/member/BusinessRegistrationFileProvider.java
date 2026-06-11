package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.business_registration_file.entity.BusinessRegistrationFile;
import com.motd.be.module.member.business_registration_file.repository.BusinessRegistrationFileRepository;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.shared.aws.enums.UploadFileType;

@Component
public class BusinessRegistrationFileProvider {

	@Autowired
	private BusinessRegistrationFileRepository businessRegistrationFileRepository;

	public BusinessRegistrationFile save(Member member) {
		return businessRegistrationFileRepository.save(BusinessRegistrationFile.builder()
			.member(member)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.sortOrder(0)
			.fileKey(FILE_KEY_STR)
			.fileType(UploadFileType.IMAGE)
			.build());
	}
}
