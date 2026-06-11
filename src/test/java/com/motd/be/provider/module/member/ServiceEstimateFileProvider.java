package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateFile;
import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateType;
import com.motd.be.module.member.service_estimate_file.repository.ServiceEstimateFileRepository;
import com.motd.be.module.member.service_estimate_template.entity.ServiceEstimateTemplate;
import com.motd.be.shared.aws.enums.UploadFileType;

@Component
public class ServiceEstimateFileProvider {

	@Autowired
	private ServiceEstimateFileRepository serviceEstimateFileRepository;

	public ServiceEstimateFile saveWithEstimateType(Member member) {
		return serviceEstimateFileRepository.save(ServiceEstimateFile.builder()
			.member(member)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.sortOrder(0)
			.fileKey(FILE_KEY_STR)
			.estimateType(ServiceEstimateType.ESTIMATE)
			.fileType(UploadFileType.IMAGE)
			.build());
	}

	public ServiceEstimateFile saveWithServiceEstimateTemplate(Member member,
		ServiceEstimateTemplate serviceEstimateTemplate, int sortOrder) {
		return serviceEstimateFileRepository.save(ServiceEstimateFile.builder()
			.member(member)
			.serviceEstimateTemplate(serviceEstimateTemplate)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.sortOrder(sortOrder)
			.fileKey(FILE_KEY_STR)
			.estimateType(ServiceEstimateType.TEMPLATE)
			.fileType(UploadFileType.IMAGE)
			.build());
	}

	public ServiceEstimateFile saveWithServiceEstimateTemplateWithIsDeletedTrue(Member member,
		ServiceEstimateTemplate serviceEstimateTemplate, int sortOrder) {
		return serviceEstimateFileRepository.save(ServiceEstimateFile.builder()
			.member(member)
			.serviceEstimateTemplate(serviceEstimateTemplate)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.sortOrder(sortOrder)
			.fileKey(FILE_KEY_STR)
			.estimateType(ServiceEstimateType.TEMPLATE)
			.fileType(UploadFileType.IMAGE)
			.isDeleted(Boolean.TRUE)
			.build());
	}

	public List<ServiceEstimateFile> findAll() {
		return serviceEstimateFileRepository.findAll();
	}
}
