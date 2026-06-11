package com.motd.be.module.member.service_estimate_file.entity;

import static com.motd.be.shared.aws.util.ImageUrlConverter.*;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.file.entity.BaseFile;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate_template.entity.ServiceEstimateTemplate;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class ServiceEstimateFile extends BaseFile {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_estimate_template_id")
	private ServiceEstimateTemplate serviceEstimateTemplate;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_estimate_id")
	private ServiceEstimate serviceEstimate;
	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private ServiceEstimateType estimateType;

	@Builder
	public ServiceEstimateFile(ServiceEstimateTemplate serviceEstimateTemplate, String originUrl,
		String cdnUrl, Boolean isDeleted, String fileKey, Member member, Integer sortOrder,
		ServiceEstimate serviceEstimate, ServiceEstimateType estimateType, UploadFileType fileType, String fileName,
		String fileSize) {
		this.serviceEstimateTemplate = serviceEstimateTemplate;
		this.originUrl = originUrl;
		this.cdnUrl = cdnUrl;
		this.isDeleted = isDeleted;
		this.fileKey = fileKey;
		this.member = member;
		this.sortOrder = sortOrder;
		this.serviceEstimate = serviceEstimate;
		this.estimateType = estimateType;
		this.fileType = fileType;
		this.fileName = fileName;
		this.fileSize = fileSize;
	}

	public static ServiceEstimateFile ofWithoutServiceEstimate(String originUrl, String fileKey, Member member,
		UploadFileType fileType, String fileName, String fileSize) {
		return ServiceEstimateFile.builder()
			.originUrl(originUrl)
			.fileKey(fileKey)
			.member(member)
			.cdnUrl(toCdnUrl(originUrl))
			.sortOrder(0)
			.fileType(fileType)
			.fileName(fileName)
			.fileSize(fileSize)
			.build();
	}

	public static ServiceEstimateFile ofWithServiceEstimate(ServiceEstimate serviceEstimate, Member member,
		ServiceEstimateFile serviceEstimateFile) {
		ServiceEstimateFile estimateFile = ServiceEstimateFile.builder()
			.originUrl(serviceEstimateFile.getOriginUrl())
			.fileKey(serviceEstimateFile.getFileKey())
			.member(member)
			.cdnUrl(serviceEstimateFile.getCdnUrl())
			.sortOrder(serviceEstimateFile.getSortOrder())
			.serviceEstimate(serviceEstimate)
			.estimateType(ServiceEstimateType.ESTIMATE)
			.fileType(serviceEstimateFile.getFileType())
			.isDeleted(Boolean.FALSE)
			.fileName(serviceEstimateFile.getFileName())
			.fileSize(serviceEstimateFile.getFileSize())
			.build();

		estimateFile.updateProcessStatus(serviceEstimateFile.getProcessStatus());
		return estimateFile;
	}

	public static ServiceEstimateFile ofWithServiceEstimateTemplate(ServiceEstimateFile image,
		ServiceEstimateTemplate serviceEstimateTemplate) {
		ServiceEstimateFile estimateFile = ServiceEstimateFile.builder()
			.originUrl(image.getOriginUrl())
			.fileKey(image.getFileKey())
			.member(image.getMember())
			.cdnUrl(image.getCdnUrl())
			.sortOrder(image.getSortOrder())
			.serviceEstimateTemplate(serviceEstimateTemplate)
			.estimateType(ServiceEstimateType.TEMPLATE)
			.fileType(image.getFileType())
			.fileName(image.getFileName())
			.fileSize(image.getFileSize())
			.build();

		estimateFile.updateProcessStatus(image.getProcessStatus());
		return estimateFile;
	}
}
