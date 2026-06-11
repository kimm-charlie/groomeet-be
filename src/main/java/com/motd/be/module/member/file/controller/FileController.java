package com.motd.be.module.member.file.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.file.dto.request.FileDeleteRequest;
import com.motd.be.module.member.file.dto.request.FileProcessStatusRequest;
import com.motd.be.module.member.file.dto.request.FileUpdateProcessRequest;
import com.motd.be.module.member.file.dto.request.FileUploadRequest;
import com.motd.be.module.member.file.dto.response.FileProcessStatusResponse;
import com.motd.be.module.member.file.dto.response.FileUploadResponse;
import com.motd.be.module.member.file.facade.FileFacade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FileController {

	private final FileFacade fileFacade;

	/**
	 * 이미지 업로드용 Presigned URL 생성
	 */
	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@PostMapping("/files/presigned-url")
	public ResponseEntity<FileUploadResponse> createPresignedUrl(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated FileUploadRequest requestDto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(fileFacade.createPresignedUrl(memberId, requestDto));
	}

	/**
	 * 이미지 처리 상태 조회
	 */
	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@PostMapping("/files/process-status")
	public ResponseEntity<FileProcessStatusResponse> getProcessStatus(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated FileProcessStatusRequest request) {
		return ResponseEntity.ok(fileFacade.getProcessStatus(memberId, request));
	}

	/**
	 * 이미지 삭제
	 */
	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@DeleteMapping("/files")
	public ResponseEntity<Void> deleteByIds(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated FileDeleteRequest request) {
		fileFacade.deleteByIds(memberId, request);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 이미지 처리 상태 업데이트 (Lambda 콜백)
	 */
	@PostMapping("/files/processed")
	public ResponseEntity<Void> updateProcessStatus(@RequestHeader(X_API_KEY) String apiKey,
		@RequestBody @Validated FileUpdateProcessRequest request) {
		fileFacade.updateProcessStatus(apiKey, request);
		return ResponseEntity.noContent().build();
	}
}
