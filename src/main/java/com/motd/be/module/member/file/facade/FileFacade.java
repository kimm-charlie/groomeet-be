package com.motd.be.module.member.file.facade;

import static com.motd.be.shared.aws.util.S3Utils.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.file.dto.request.FileDeleteRequest;
import com.motd.be.module.member.file.dto.request.FileProcessStatusRequest;
import com.motd.be.module.member.file.dto.request.FileUpdateProcessRequest;
import com.motd.be.module.member.file.dto.request.FileUploadRequest;
import com.motd.be.module.member.file.dto.response.FileProcessStatusResponse;
import com.motd.be.module.member.file.dto.response.FileUploadResponse;
import com.motd.be.module.member.file.entity.BaseFile;
import com.motd.be.module.member.file.enums.FileProcessStatus;
import com.motd.be.module.member.file.service.FileService;
import com.motd.be.module.member.file.validator.FileValidator;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.shared.aws.dto.GeneratedPresignedUrl;
import com.motd.be.shared.aws.enums.S3DirectoryType;
import com.motd.be.shared.aws.enums.UploadFileType;
import com.motd.be.shared.aws.service.S3PresignedUrlService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileFacade {

	private final MemberQueryService memberQueryService;
	private final S3PresignedUrlService s3PresignedUrlService;
	private final FileService fileService;
	private final FileValidator fileValidator;

	/**
	 * 이미지 업로드용 Presigned URL 생성 및 DB 저장
	 */
	@Transactional
	public FileUploadResponse createPresignedUrl(Long memberId, FileUploadRequest request) {
		// 1. 업로더(멤버) 조회
		Member member = memberQueryService.findById(memberId);

		// 2. 디렉터리 타입 확인
		S3DirectoryType directoryType = S3DirectoryType.findByName(request.getDirectoryType());

		// 3. 파일 타입 검증
		UploadFileType uploadFileType = UploadFileType.from(request.getFileType());
		uploadFileType.validateExtension(request.getFileExtension());
		directoryType.validateFileType(uploadFileType);

		// 파일 용량 검증
		uploadFileType.validateFileSize(request.getFileSize());

		// 4. Presigned URL 생성 (파일 크기에 따라 버킷 결정)
		long fileSizeBytes = parseFileSizeOrDefault(request.getFileSize());
		GeneratedPresignedUrl presignedUrl = s3PresignedUrlService.generatePresignedUrl(directoryType, uploadFileType,
			request.getFileExtension(), fileSizeBytes);

		// 5. 타입에 맞는 이미지 저장 처리
		BaseFile savedImage = fileService.saveImageByType(directoryType, presignedUrl, member, uploadFileType,
			request.getFileName(), request.getFileSize());

		// 6. Public 버킷에 저장되는 파일은 processed를 true로 설정
		if (presignedUrl.isPublicBucket()) {
			savedImage.updateProcessStatus(FileProcessStatus.PROCESSED);
		}

		// 7. 응답 조립
		return FileUploadResponse.of(presignedUrl.getPresignedUrl(), savedImage);
	}

	/**
	 * 이미지 삭제 (논리 삭제 + 권한 검증)
	 */
	@Transactional
	public void deleteByIds(Long memberId, FileDeleteRequest request) {
		// 1. 디렉터리 타입 판별
		S3DirectoryType directoryType = S3DirectoryType.findByName(request.getDirectoryType());

		// 2. 해당 타입의 이미지 목록 조회
		List<? extends BaseFile> images = fileService.findImagesByType(directoryType, request.getFileIds());

		// 3. 소유자 검증 및 논리 삭제
		fileService.delete(images, memberId);

		// 4. (선택) S3 삭제 처리
		// s3DeleteService.deleteImages(images);
	}

	public FileProcessStatusResponse getProcessStatus(Long memberId, FileProcessStatusRequest request) {
		S3DirectoryType directoryType = S3DirectoryType.findByName(request.getDirectoryType());

		FileProcessStatus processStatus = fileService.getProcessStatus(directoryType, request.getFileId(), memberId);

		return FileProcessStatusResponse.of(request.getFileId(), directoryType, processStatus);
	}

	@Transactional
	public void updateProcessStatus(String apiKey, FileUpdateProcessRequest request) {
		// api key 검증
		fileValidator.validateApiKey(apiKey);

		// 처리 상태 업데이트
		fileService.updateProcessStatus(request.getFileKey(), request);
	}
}
