package com.motd.be.module.director.file.dto.response;

import java.util.List;

import com.motd.be.module.member.business_registration_file.entity.BusinessRegistrationFile;
import com.motd.be.module.member.file.entity.BaseFile;
import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateFile;
import com.motd.be.module.member.service_request_file.entity.ServiceRequestFile;
import com.motd.be.shared.aws.enums.UploadFileType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileResponseForDirector {

	private Long id;
	private String fileUrl;
	private UploadFileType fileType;
	private String fileName;
	private String fileSize;

	public static FileResponseForDirector from(BaseFile baseFile) {
		return FileResponseForDirector.builder()
			.id(baseFile.getId())
			.fileUrl(baseFile.getCdnUrl())
			.fileType(baseFile.getFileType())
			.fileName(baseFile.getFileName())
			.fileSize(baseFile.getFileSize())
			.build();
	}

	public static List<FileResponseForDirector> fromListWithEstimateFiles(List<ServiceEstimateFile> files) {
		return files.stream().map(FileResponseForDirector::from).toList();
	}

	public static List<FileResponseForDirector> fromListWithBusinessRegistrationFiles(
		List<BusinessRegistrationFile> files) {
		return files.stream().map(FileResponseForDirector::from).toList();
	}

	public static List<FileResponseForDirector> fromListWithServiceRequestFiles(List<ServiceRequestFile> files) {
		return files.stream().map(FileResponseForDirector::from).toList();
	}
}
