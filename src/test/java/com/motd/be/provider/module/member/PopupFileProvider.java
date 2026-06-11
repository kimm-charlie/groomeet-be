package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.admin.admin.entity.Admin;
import com.motd.be.module.member.popup_file.entity.PopupFile;
import com.motd.be.module.member.popup_file.repository.PopupFileRepository;
import com.motd.be.shared.aws.enums.S3DirectoryType;
import com.motd.be.shared.aws.enums.UploadFileType;

@Component
public class PopupFileProvider {

	@Autowired
	private PopupFileRepository popupFileRepository;

	public PopupFile save(Admin admin, S3DirectoryType s3DirectoryType) {
		return popupFileRepository.save(PopupFile.builder()
			.admin(admin)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.fileKey(FILE_KEY_STR)
			.fileType(UploadFileType.IMAGE)
			.directoryType(s3DirectoryType)
			.build());
	}

	public PopupFile findById(Long id) {
		return popupFileRepository.findById(id).orElseThrow();
	}
}
