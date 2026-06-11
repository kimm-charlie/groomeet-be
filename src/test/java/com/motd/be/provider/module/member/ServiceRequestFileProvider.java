package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request_file.entity.ServiceRequestFile;
import com.motd.be.module.member.service_request_file.repository.ServiceRequestFileRepository;
import com.motd.be.shared.aws.enums.UploadFileType;

@Component
public class ServiceRequestFileProvider {

	@Autowired
	private ServiceRequestFileRepository serviceRequestFileRepository;

	public ServiceRequestFile saveWithoutServiceRequest(Member member) {
		return serviceRequestFileRepository.save(ServiceRequestFile.builder()
			.member(member)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.fileKey(FILE_KEY_STR)
			.isDeleted(Boolean.FALSE)
			.sortOrder(0)
			.fileType(UploadFileType.IMAGE)
			.build());
	}

	public ServiceRequestFile saveWithServiceRequest(Member member, ServiceRequest serviceRequest, int sortOrder) {
		return serviceRequestFileRepository.save(ServiceRequestFile.builder()
			.member(member)
			.serviceRequest(serviceRequest)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.fileKey(FILE_KEY_STR)
			.isDeleted(Boolean.FALSE)
			.sortOrder(sortOrder)
			.fileType(UploadFileType.IMAGE)
			.build());
	}

	public List<ServiceRequestFile> findAll() {
		return serviceRequestFileRepository.findAll();
	}

	public ServiceRequestFile saveWithServiceRequestWithIsDeletedTrue(Member member, ServiceRequest serviceRequest,
		int sortOrder) {
		return serviceRequestFileRepository.save(ServiceRequestFile.builder()
			.member(member)
			.serviceRequest(serviceRequest)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.fileKey(FILE_KEY_STR)
			.isDeleted(Boolean.TRUE)
			.sortOrder(sortOrder)
			.fileType(UploadFileType.IMAGE)
			.build());
	}
}
