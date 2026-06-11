package com.motd.be.module.member.file.dto.response;

import java.util.List;

import com.motd.be.module.member.chat_file.entity.ChatFile;
import com.motd.be.module.member.file.entity.BaseFile;
import com.motd.be.module.member.review_file.entity.ReviewFile;
import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateFile;
import com.motd.be.module.member.consulting_request_file.entity.ConsultingRequestFile;
import com.motd.be.module.member.consulting_sheet_file.entity.ConsultingSheetFile;
import com.motd.be.module.member.service_request_file.entity.ServiceRequestFile;
import com.motd.be.shared.aws.enums.UploadFileType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileResponse {

	private Long id;
	private String fileUrl;
	private UploadFileType fileType;
	private String fileName;
	private String fileSize;

	public static FileResponse from(BaseFile baseFile) {
		return FileResponse.builder()
			.id(baseFile.getId())
			.fileUrl(baseFile.getCdnUrl())
			.fileType(baseFile.getFileType())
			.fileName(baseFile.getFileName())
			.fileSize(baseFile.getFileSize())
			.build();
	}

	public static List<FileResponse> fromListWithChatFiles(List<ChatFile> files) {
		return files.stream().map(FileResponse::from).toList();
	}

	public static List<FileResponse> fromListWithEstimateFiles(List<ServiceEstimateFile> files) {
		return files.stream().map(FileResponse::from).toList();
	}

	public static List<FileResponse> fromListWithReviewFiles(List<ReviewFile> files) {
		return files.stream().map(FileResponse::from).toList();
	}

	public static List<FileResponse> fromListWithServiceRequestFiles(List<ServiceRequestFile> files) {
		return files.stream().map(FileResponse::from).toList();
	}

	public static List<FileResponse> fromListWithConsultingSheetFiles(List<ConsultingSheetFile> files) {
		return files.stream().map(FileResponse::from).toList();
	}

	public static List<FileResponse> fromListWithConsultingRequestFiles(List<ConsultingRequestFile> files) {
		return files.stream().map(FileResponse::from).toList();
	}
}
