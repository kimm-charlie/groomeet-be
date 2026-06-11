package com.motd.be.module.member.consulting_sheet_file.entity;

import static com.motd.be.shared.aws.util.ImageUrlConverter.*;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
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
public class ConsultingSheetFile extends BaseFile {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "consulting_sheet_id")
	private ConsultingSheet consultingSheet;

	@Builder
	public ConsultingSheetFile(ConsultingSheet consultingSheet, String originUrl, String cdnUrl, String fileKey,
		Member member, Integer sortOrder, UploadFileType fileType, String fileName, String fileSize) {
		this.consultingSheet = consultingSheet;
		this.originUrl = originUrl;
		this.cdnUrl = cdnUrl;
		this.fileKey = fileKey;
		this.member = member;
		this.sortOrder = sortOrder;
		this.fileType = fileType;
		this.fileName = fileName;
		this.fileSize = fileSize;
	}

	public static ConsultingSheetFile ofWithoutConsultingSheet(String originUrl, String fileKey, Member member,
		UploadFileType fileType, String fileName, String fileSize) {
		return ConsultingSheetFile.builder()
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
