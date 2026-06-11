package com.motd.be.module.member.business_registration_file.entity;

import static com.motd.be.shared.aws.util.ImageUrlConverter.*;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.business_registration.entity.BusinessRegistration;
import com.motd.be.module.member.file.entity.BaseFile;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class BusinessRegistrationFile extends BaseFile {
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "business_registration_id")
	private BusinessRegistration businessRegistration;

	@Builder
	public BusinessRegistrationFile(BusinessRegistration businessRegistration, String originUrl, String cdnUrl,
		Boolean isDeleted, String fileKey, Member member, Integer sortOrder, UploadFileType fileType,
		String fileName, String fileSize) {
		this.businessRegistration = businessRegistration;
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

	public static BusinessRegistrationFile ofWithoutBusinessRegistration(String originUrl, String fileKey,
		Member member, UploadFileType fileType, String fileName, String fileSize) {
		return BusinessRegistrationFile.builder()
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
