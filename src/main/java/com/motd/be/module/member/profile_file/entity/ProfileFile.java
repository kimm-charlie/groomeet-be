package com.motd.be.module.member.profile_file.entity;

import static com.motd.be.shared.aws.util.ImageUrlConverter.*;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.file.entity.BaseFile;
import com.motd.be.module.member.member.entity.Member;
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
public class ProfileFile extends BaseFile {

	@Builder
	public ProfileFile(String originUrl, String cdnUrl, Boolean isDeleted, String fileKey, Member member,
		Integer sortOrder,
		UploadFileType fileType, String fileName, String fileSize) {
		this.originUrl = originUrl;
		this.cdnUrl = cdnUrl;
		this.isDeleted = isDeleted;
		this.fileKey = fileKey;
		this.member = member;
		this.sortOrder = sortOrder;
		this.fileType = fileType;
		this.fileName = fileName;
		this.fileSize = fileSize;
	}

	public static ProfileFile of(String originUrl, String fileKey,
		Member member, UploadFileType fileType, String fileName, String fileSize) {
		return ProfileFile.builder()
			.originUrl(originUrl)
			.cdnUrl(toCdnUrl(originUrl))
			.fileKey(fileKey)
			.member(member)
			.sortOrder(0)
			.fileType(fileType)
			.fileName(fileName)
			.fileSize(fileSize)
			.build();
	}
}
