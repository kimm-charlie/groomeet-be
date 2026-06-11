package com.motd.be.module.admin.admin_file.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.admin.AdminTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.module.admin.admin.entity.Admin;
import com.motd.be.module.admin.admin_file.dto.request.AdminFileDeleteRequestForAdmin;
import com.motd.be.module.admin.admin_file.dto.request.AdminFileUploadRequestForAdmin;
import com.motd.be.module.admin.admin_file.dto.response.AdminFileUploadResponseForAdmin;
import com.motd.be.module.member.banner_file.entity.BannerFile;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.shared.aws.dto.GeneratedPresignedUrl;
import com.motd.be.shared.aws.enums.S3DirectoryType;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class AdminFileControllerTest extends BaseIntegrationTest {

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
	@DisplayName("관리자는 파일 업로드용 Presigned URL을 발급받을 수 있다.")
	void createPresignedUrl_success() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		AdminFileUploadRequestForAdmin request = AdminFileUploadRequestForAdmin.builder()
			.directoryType(S3DirectoryType.MEMBER_BANNER_THUMBNAIL.name())
			.fileType(UploadFileType.IMAGE.name())
			.fileExtension("jpg")
			.fileName("test-banner")
			.fileSize("5")
			.build();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/admin/files/presigned-url")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andReturn();

		// then
		AdminFileUploadResponseForAdmin response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			AdminFileUploadResponseForAdmin.class);

		assertThat(response.getPresignedUrl()).isNotNull();
		assertThat(response.getImageId()).isNotNull();
		assertThat(response.getCdnUrl()).isNotNull();
	}

	@Test
	@DisplayName("관리자는 배너 파일을 삭제할 수 있다.")
	void deleteByIds_banner_success() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		BannerFile bannerFile1 = bannerFileProvider.save(admin,
			S3DirectoryType.MEMBER_BANNER_THUMBNAIL);
		BannerFile bannerFile2 = bannerFileProvider.save(admin,
			S3DirectoryType.MEMBER_BANNER_THUMBNAIL);

		entityManager.flush();
		entityManager.clear();

		AdminFileDeleteRequestForAdmin request = AdminFileDeleteRequestForAdmin.builder()
			.fileIds(List.of(bannerFile1.getId(), bannerFile2.getId()))
			.directoryType(S3DirectoryType.MEMBER_BANNER_THUMBNAIL.name())
			.build();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/admin/files")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		BannerFile deletedFile1 = entityManager.find(BannerFile.class, bannerFile1.getId());
		BannerFile deletedFile2 = entityManager.find(BannerFile.class, bannerFile2.getId());

		assertThat(deletedFile1.getIsDeleted()).isTrue();
		assertThat(deletedFile2.getIsDeleted()).isTrue();
	}

	@Test
	@DisplayName("관리자는 다른 관리자의 파일을 삭제할 수 없다.")
	void deleteByIds_unauthorized_fail() throws Exception {
		// given
		Admin admin1 = adminProvider.save("admin1@email.com", PASSWORD);
		Admin admin2 = adminProvider.save("admin2@email.com", PASSWORD);

		BannerFile bannerFile = bannerFileProvider.save(admin1,
			S3DirectoryType.MEMBER_BANNER_THUMBNAIL);

		entityManager.flush();
		entityManager.clear();

		AdminFileDeleteRequestForAdmin request = AdminFileDeleteRequestForAdmin.builder()
			.fileIds(List.of(bannerFile.getId()))
			.directoryType(S3DirectoryType.MEMBER_BANNER_THUMBNAIL.name())
			.build();

		Jwt jwt = generateAdminTokenWithAdminId(admin2.getId());

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/admin/files")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden());

		entityManager.flush();
		entityManager.clear();

		BannerFile notDeletedFile = entityManager.find(BannerFile.class, bannerFile.getId());
		assertThat(notDeletedFile.getIsDeleted()).isFalse();
	}

	@Test
	@DisplayName("관리자는 유효하지 않은 파일 타입으로 Presigned URL 발급 시 실패한다.")
	void createPresignedUrl_invalidFileType_fail() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		AdminFileUploadRequestForAdmin request = AdminFileUploadRequestForAdmin.builder()
			.directoryType(S3DirectoryType.MEMBER_BANNER_THUMBNAIL.name())
			.fileType(UploadFileType.IMAGE.name())
			.fileExtension("exe") // 허용되지 않는 확장자
			.fileName("test-file")
			.fileSize("5MB")
			.build();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/admin/files/presigned-url")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}
}
