package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.admin.admin.entity.Admin;
import com.motd.be.module.member.banner_file.entity.BannerFile;
import com.motd.be.module.member.banner_file.repository.BannerFileRepository;
import com.motd.be.shared.aws.enums.S3DirectoryType;
import com.motd.be.shared.aws.enums.UploadFileType;

@Component
public class BannerFileProvider {

	@Autowired
	private BannerFileRepository bannerFileRepository;

	public BannerFile save(Admin admin, S3DirectoryType s3DirectoryType) {
		return bannerFileRepository.save(BannerFile.builder()
			.admin(admin)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.fileKey(FILE_KEY_STR)
			.fileType(UploadFileType.IMAGE)
			.s3DirectoryType(s3DirectoryType)
			.build());
	}

	public BannerFile findById(Long id) {
		return bannerFileRepository.findById(id).orElseThrow();
	}
}
