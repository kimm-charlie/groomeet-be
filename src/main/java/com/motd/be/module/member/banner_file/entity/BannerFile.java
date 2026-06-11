package com.motd.be.module.member.banner_file.entity;

import static com.motd.be.shared.aws.util.ImageUrlConverter.*;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.admin.admin.entity.Admin;
import com.motd.be.module.admin.admin_file.entity.AdminFileForAdmin;
import com.motd.be.shared.aws.enums.S3DirectoryType;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class BannerFile extends AdminFileForAdmin {

	@Builder
	public BannerFile(String originUrl, String cdnUrl, Boolean isDeleted, String fileKey, Admin admin,
		UploadFileType fileType, String fileName, String fileSize, S3DirectoryType s3DirectoryType) {
		this.admin = admin;
		this.originUrl = originUrl;
		this.cdnUrl = cdnUrl;
		this.fileKey = fileKey;
		this.isDeleted = isDeleted;
		this.fileType = fileType;
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.directoryType = s3DirectoryType;
	}

	public static BannerFile of(String originUrl, String fileKey, Admin admin, UploadFileType fileType,
		String fileName, String fileSize, S3DirectoryType type) {
		return BannerFile.builder()
			.originUrl(originUrl)
			.cdnUrl(toCdnUrl(originUrl))
			.fileKey(fileKey)
			.admin(admin)
			.fileType(fileType)
			.fileName(fileName)
			.fileSize(fileSize)
			.s3DirectoryType(type)
			.build();
	}

	public void deleteIfNeeded(BannerFile newFile) {
		if (newFile == null) {
			this.delete();
		}

		if (!this.getId().equals(newFile.getId())) {
			this.delete();
		}
	}
}
