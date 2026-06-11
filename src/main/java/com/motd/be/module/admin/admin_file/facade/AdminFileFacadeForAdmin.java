package com.motd.be.module.admin.admin_file.facade;

import static com.motd.be.shared.aws.util.S3Utils.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.admin.admin.entity.Admin;
import com.motd.be.module.admin.admin.service.AdminQueryService;
import com.motd.be.module.admin.admin_file.dto.request.AdminFileDeleteRequestForAdmin;
import com.motd.be.module.admin.admin_file.dto.request.AdminFileUpdateProcessRequestForAdmin;
import com.motd.be.module.admin.admin_file.dto.request.AdminFileUploadRequestForAdmin;
import com.motd.be.module.admin.admin_file.dto.response.AdminFileUploadResponseForAdmin;
import com.motd.be.module.admin.admin_file.entity.AdminFileForAdmin;
import com.motd.be.module.admin.admin_file.service.AdminFileServiceForAdmin;
import com.motd.be.module.admin.admin_file.validator.AdminFileValidatorForAdmin;
import com.motd.be.module.member.file.enums.FileProcessStatus;
import com.motd.be.shared.aws.dto.GeneratedPresignedUrl;
import com.motd.be.shared.aws.enums.S3DirectoryType;
import com.motd.be.shared.aws.enums.UploadFileType;
import com.motd.be.shared.aws.service.S3PresignedUrlService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminFileFacadeForAdmin {

	private final AdminQueryService adminQueryService;
	private final S3PresignedUrlService s3PresignedUrlService;
	private final AdminFileServiceForAdmin adminFileServiceForAdmin;
	private final AdminFileValidatorForAdmin adminFileValidatorForAdmin;

	@Transactional
	public AdminFileUploadResponseForAdmin createPresignedUrl(Long adminId, AdminFileUploadRequestForAdmin request) {
		// 1. 업로더(어드민) 조회
		Admin admin = adminQueryService.findById(adminId);

		// 2. 디렉터리 타입 확인
		S3DirectoryType directoryType = S3DirectoryType.findByName(request.getDirectoryType());

		// 3. 파일 타입 검증
		UploadFileType uploadFileType = UploadFileType.from(request.getFileType());
		uploadFileType.validateExtension(request.getFileExtension());
		directoryType.validateFileType(uploadFileType);

		// 파일 용량 검증
		uploadFileType.validateFileSize(request.getFileSize());

		// Presigned URL 생성 (파일 크기에 따라 버킷 결정)
		long fileSizeBytes = parseFileSizeOrDefault(request.getFileSize());
		GeneratedPresignedUrl presignedUrl = s3PresignedUrlService.generatePresignedUrl(directoryType, uploadFileType,
			request.getFileExtension(), fileSizeBytes);

		AdminFileForAdmin savedFile = adminFileServiceForAdmin.saveImageByType(directoryType, presignedUrl, admin,
			uploadFileType,
			request.getFileName(), request.getFileSize());

		// Public 버킷에 저장되는 파일은 processed를 true로 설정
		if (presignedUrl.isPublicBucket()) {
			savedFile.updateProcessStatus(FileProcessStatus.PROCESSED);
		}

		return AdminFileUploadResponseForAdmin.of(presignedUrl.getPresignedUrl(), savedFile);
	}

	@Transactional
	public void deleteByIds(Long adminId, AdminFileDeleteRequestForAdmin request) {
		// 디렉터리 타입 확인
		S3DirectoryType directoryType = S3DirectoryType.findByName(request.getDirectoryType());

		// 해당 타입의 이미지 목록 조회
		List<? extends AdminFileForAdmin> files = adminFileServiceForAdmin.findFilesByType(directoryType,
			request.getFileIds());

		adminFileServiceForAdmin.delete(files, adminId);
	}

	@Transactional
	public void updateProcessStatus(String apiKey, AdminFileUpdateProcessRequestForAdmin request) {
		// api key 검증
		adminFileValidatorForAdmin.validateApiKey(apiKey);

		// 처리 상태 업데이트
		adminFileServiceForAdmin.updateProcessStatus(request.getFileKey(), request);
	}
}
