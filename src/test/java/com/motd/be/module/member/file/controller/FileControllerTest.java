package com.motd.be.module.member.file.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.AwsException;
import com.motd.be.exception.exceptions.FileException;
import com.motd.be.exception.exceptions.ImageException;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.file.dto.request.FileDeleteRequest;
import com.motd.be.module.member.file.dto.request.FileProcessStatusRequest;
import com.motd.be.module.member.file.dto.request.FileUpdateProcessRequest;
import com.motd.be.module.member.file.dto.request.FileUploadRequest;
import com.motd.be.module.member.file.dto.response.FileUploadResponse;
import com.motd.be.module.member.file.enums.FileProcessStatus;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;
import com.motd.be.shared.aws.dto.GeneratedPresignedUrl;
import com.motd.be.shared.aws.enums.S3DirectoryType;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class FileControllerTest extends BaseIntegrationTest {

	@BeforeEach
	public void setUp() {
		given(s3PresignedUrlService.generatePresignedUrl(any(S3DirectoryType.class),
			any(UploadFileType.class), any(String.class), anyLong())).willReturn(
			GeneratedPresignedUrl.builder()
				.originUrl(ORIGIN_URL_STR)
				.presignedUrl(PRESIGNED_URL)
				.fileKey(FILE_KEY_STR)
				.build());
	}

	@Test
	@DisplayName("presignedUrl 을 발급할 수 있다.")
	void createPresignedUrl() throws Exception {
		// given
		//1. 회원 저장
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		FileUploadRequest request = FileUploadRequest.builder()
			.directoryType(S3DirectoryType.PORTFOLIO.name())
			.fileType(UploadFileType.IMAGE.name())
			.fileExtension(UploadFileType.IMAGE.getAllowedExtensions()
				.iterator()
				.next())
			.fileName(null)
			.fileSize(String.valueOf(FILE_SIZE))
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(post("/api/files/presigned-url")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().isCreated())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		// then

		// portfolioImage 가 저장되었는지 확인한다.
		List<PortfolioFile> portfolioImages = portfolioFileProvider.findAll();
		assertThat(portfolioImages).hasSize(1);
		PortfolioFile portfolioImage = portfolioImages.get(0);

		String content = result.getResponse().getContentAsString();
		FileUploadResponse response = objectMapper.readValue(content, FileUploadResponse.class);

		assertThat(response.getPresignedUrl()).isEqualTo(PRESIGNED_URL);
		assertThat(response.getImageId()).isEqualTo(portfolioImage.getId());
	}

	@Test
	@DisplayName("presignedUrl 을 발급할 수 있다. (유효하지않은 S3DirectoryType 일때)")
	void createPresignedUrlWithInvalidS3DirectoryType() throws Exception {
		// given
		//1. 회원 저장
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		FileUploadRequest request = FileUploadRequest.builder()
			.directoryType(S3DirectoryType.PORTFOLIO.name() + "SS")
			.fileType(UploadFileType.IMAGE.name())
			.fileExtension(UploadFileType.IMAGE.getAllowedExtensions()
				.iterator()
				.next())
			.fileName(null)
			.fileSize(String.valueOf(FILE_SIZE))
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		// when && then
		mockMvc.perform(post("/api/files/presigned-url")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(AwsException.S3_DIRECTORY_TYPE_NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(AwsException.S3_DIRECTORY_TYPE_NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(AwsException.S3_DIRECTORY_TYPE_NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("presignedUrl 을 발급할 수 있다. (유효하지않은 파일일때 ex) 이미지만 업로드 가능인데, 파일을 올릴때)")
	void createPresignedUrlWithNotAllowedFileType() throws Exception {
		// given
		//1. 회원 저장
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		FileUploadRequest request = FileUploadRequest.builder()
			.directoryType(S3DirectoryType.PORTFOLIO.name())
			.fileType(UploadFileType.DOCUMENT.name())
			.fileExtension(UploadFileType.DOCUMENT.getAllowedExtensions()
				.iterator()
				.next())
			.fileName(FILE_NAME_STR)
			.fileSize(String.valueOf(FILE_SIZE))
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		// when && then
		mockMvc.perform(post("/api/files/presigned-url")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(AwsException.INVALID_FILE_TYPE.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(AwsException.INVALID_FILE_TYPE.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(AwsException.INVALID_FILE_TYPE.getCode()));
	}

	@Test
	@DisplayName("presignedUrl 을 발급할 수 없다. (파일 사이즈가 초과되었을떄)")
	void createPresignedUrlWithExceededFileSize() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		FileUploadRequest request = FileUploadRequest.builder()
			.directoryType(S3DirectoryType.PORTFOLIO.name())
			.fileType(UploadFileType.IMAGE.name())
			.fileExtension(UploadFileType.IMAGE.getAllowedExtensions()
				.iterator()
				.next())
			.fileName(null)
			.fileSize(String.valueOf(FILE_SIZE * 10))
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		// when && then
		mockMvc.perform(post("/api/files/presigned-url")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(AwsException.FILE_SIZE_EXCEEDED.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(AwsException.FILE_SIZE_EXCEEDED.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(AwsException.FILE_SIZE_EXCEEDED.getCode()));
	}

	@Test
	@DisplayName("presignedUrl 을 발급할 수 없다. (지원하지 않는 파일 유형)")
	void createPresignedUrlWithInvalidFileType() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		FileUploadRequest request = FileUploadRequest.builder()
			.directoryType(S3DirectoryType.PORTFOLIO.name())
			.fileType(UploadFileType.DOCUMENT.name())
			.fileExtension(UploadFileType.IMAGE.getAllowedExtensions()
				.iterator()
				.next())
			.fileName(null)
			.fileSize(String.valueOf(FILE_SIZE))
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		// when && then
		mockMvc.perform(post("/api/files/presigned-url")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(AwsException.INVALID_FILE_EXTENSION.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(AwsException.INVALID_FILE_EXTENSION.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(AwsException.INVALID_FILE_EXTENSION.getCode()));
	}

	@Test
	@DisplayName("사용자는 자신이 올린 이미지 삭제가 가능하다.")
	void deleteByIds() throws Exception {
		// given
		//1. 회원 저장
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(member.getId());

		//2. PortfolioFile 저장
		PortfolioFile portfolioImage1 = portfolioFileProvider.save(member, null, true);
		PortfolioFile portfolioImage2 = portfolioFileProvider.save(member, null, false);
		PortfolioFile portfolioImage3 = portfolioFileProvider.save(member, null, false);

		FileDeleteRequest request = FileDeleteRequest.builder()
			.directoryType(S3DirectoryType.PORTFOLIO.name())
			.fileIds(Arrays.asList(portfolioImage2.getId(), portfolioImage3.getId()))
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(delete("/api/files")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then

		//tempImage 가 삭제되었는지 확인한다.
		List<PortfolioFile> portfolioImages = portfolioFileProvider.findAll();

		assertThat(portfolioImages).hasSize(3);

		List<PortfolioFile> deletedTempImages = portfolioImages.stream()
			.filter(PortfolioFile::getIsDeleted)
			.toList();

		assertThat(deletedTempImages).hasSize(2);

		List<Long> deletedTempFileIds = deletedTempImages.stream()
			.map(PortfolioFile::getId)
			.toList();

		assertThat(deletedTempFileIds.contains(portfolioImage2.getId())).isTrue();
		assertThat(deletedTempFileIds.contains(portfolioImage3.getId())).isTrue();
	}

	@Test
	@DisplayName("사용자는 자신이 올린 이미지 삭제가 가능하다.(자신이 올리지 않은 이미지가 포함되어 있을때)")
	void deleteByIdsWithNotOwnedImage() throws Exception {
		// given
		//1. 회원 저장
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(member.getId());

		//2. PortfolioFile 저장
		PortfolioFile portfolioImage1 = portfolioFileProvider.save(member, null, true);
		PortfolioFile portfolioImage2 = portfolioFileProvider.save(member, null, false);
		PortfolioFile portfolioImage3 = portfolioFileProvider.save(otherMember, null, false);

		FileDeleteRequest request = FileDeleteRequest.builder()
			.fileIds(Arrays.asList(portfolioImage2.getId(), portfolioImage3.getId()))
			.directoryType(S3DirectoryType.PORTFOLIO.name())
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		entityManager.flush();
		entityManager.clear();

		// when && then
		mockMvc.perform(delete("/api/files")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(ImageException.NOT_OWNED_BY.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(ImageException.NOT_OWNED_BY.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(ImageException.NOT_OWNED_BY.getCode()));
	}

	@Test
	@DisplayName("이미지 처리 상태를 조회할 수 있다.")
	void getProcessStatus() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		PortfolioFile portfolioFile = portfolioFileProvider.save(member, null, false);
		portfolioFile.updateProcessStatus(FileProcessStatus.PROCESSED);
		entityManager.flush();
		entityManager.clear();

		FileProcessStatusRequest request = FileProcessStatusRequest.builder()
			.directoryType(S3DirectoryType.PORTFOLIO.name())
			.fileId(portfolioFile.getId())
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		// when && then
		mockMvc.perform(post("/api/files/process-status")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.fileId").value(portfolioFile.getId()))
			.andExpect(jsonPath("$.processStatus").value(FileProcessStatus.PROCESSED.name()));
	}

	@Test
	@DisplayName("이미지 처리 상태 조회 실패 - 파일이 존재하지 않음")
	void getProcessStatusWithFileNotFound() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		FileProcessStatusRequest request = FileProcessStatusRequest.builder()
			.directoryType(S3DirectoryType.PORTFOLIO.name())
			.fileId(999L)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		entityManager.flush();
		entityManager.clear();

		// when && then
		mockMvc.perform(post("/api/files/process-status")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(FileException.FILE_NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(FileException.FILE_NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(FileException.FILE_NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("이미지 처리 상태 조회 실패 - 자신이 소유하지 않은 파일")
	void getProcessStatusWithNotOwnedFile() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		PortfolioFile portfolioFile = portfolioFileProvider.save(otherMember, null, false);
		entityManager.flush();
		entityManager.clear();

		FileProcessStatusRequest request = FileProcessStatusRequest.builder()
			.directoryType(S3DirectoryType.PORTFOLIO.name())
			.fileId(portfolioFile.getId())
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		// when && then
		mockMvc.perform(post("/api/files/process-status")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(ImageException.NOT_OWNED_BY.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(ImageException.NOT_OWNED_BY.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(ImageException.NOT_OWNED_BY.getCode()));
	}

	@Test
	@DisplayName("이미지 처리 상태 조회 실패 - 유효하지 않은 디렉터리 타입")
	void getProcessStatusWithInvalidDirectoryType() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		FileProcessStatusRequest request = FileProcessStatusRequest.builder()
			.directoryType("INVALID_TYPE")
			.fileId(1L)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		// when && then
		mockMvc.perform(post("/api/files/process-status")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(AwsException.S3_DIRECTORY_TYPE_NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(AwsException.S3_DIRECTORY_TYPE_NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(AwsException.S3_DIRECTORY_TYPE_NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("파일 처리 상태를 업데이트할 수 있다.")
	void updateProcessStatus() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		PortfolioFile portfolioFile = portfolioFileProvider.saveWithFileKey(member, null, false,
			S3DirectoryType.PORTFOLIO.getDirectoryName() + "sample-file-key.jpg");

		FileUpdateProcessRequest request = FileUpdateProcessRequest.builder()
			.fileKey(portfolioFile.getFileKey())
			.processStatus(FileProcessStatus.PROCESSED)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(post("/api/files/processed")
				.header(X_API_KEY, "motd-test-lambda-secret-key")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		List<PortfolioFile> updatedFile = portfolioFileProvider.findAll();
		PortfolioFile portfolioFileUpdated = updatedFile.get(0);
		assertThat(portfolioFileUpdated.getProcessStatus()).isEqualTo(FileProcessStatus.PROCESSED);
	}

	@Test
	@DisplayName("파일 처리 상태 업데이트 실패 - 유효하지 않은 API 키")
	void updateProcessStatusWithInvalidApiKey() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		PortfolioFile portfolioFile = portfolioFileProvider.save(member, null, false);

		FileUpdateProcessRequest request = FileUpdateProcessRequest.builder()
			.fileKey(portfolioFile.getFileKey())
			.processStatus(FileProcessStatus.PROCESSED)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		// when && then
		mockMvc.perform(post("/api/files/processed")
				.header(X_API_KEY, "invalid-api-key")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(FileException.UNAUTHORIZED_ACCESS.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(FileException.UNAUTHORIZED_ACCESS.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(FileException.UNAUTHORIZED_ACCESS.getCode()));
	}

	@Test
	@DisplayName("파일 처리 상태 업데이트 실패 - 유효하지 않은 파일 키")
	void updateProcessStatusWithInvalidFileKey() throws Exception {
		// given
		FileUpdateProcessRequest request = FileUpdateProcessRequest.builder()
			.fileKey("invalid/file/key")
			.processStatus(FileProcessStatus.PROCESSED)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		// when && then
		mockMvc.perform(post("/api/files/processed")
				.header(X_API_KEY, "motd-test-lambda-secret-key")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath("$.status").value(FileException.INVALID_FILE_KEY.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(FileException.INVALID_FILE_KEY.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(FileException.INVALID_FILE_KEY.getCode()));
	}

	@Test
	@DisplayName("파일 처리 상태를 FAILED로 업데이트할 수 있다.")
	void updateProcessStatusToFailed() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		PortfolioFile portfolioFile = portfolioFileProvider.saveWithFileKey(member, null, false,
			S3DirectoryType.PORTFOLIO.getDirectoryName() + "sample-file-key.jpg");

		FileUpdateProcessRequest request = FileUpdateProcessRequest.builder()
			.fileKey(portfolioFile.getFileKey())
			.processStatus(FileProcessStatus.FAILED)
			.build();

		String jsonRequest = objectMapper.writeValueAsString(request);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(post("/api/files/processed")
				.header(X_API_KEY, "motd-test-lambda-secret-key")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonRequest))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		List<PortfolioFile> updatedFiles = portfolioFileProvider.findAll();
		PortfolioFile updatedFile = updatedFiles.get(0);
		assertThat(updatedFile.getProcessStatus()).isEqualTo(FileProcessStatus.FAILED);
	}

}
