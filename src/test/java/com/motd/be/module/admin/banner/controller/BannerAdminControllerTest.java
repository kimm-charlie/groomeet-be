package com.motd.be.module.admin.banner.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;
import static com.motd.be.provider.module.admin.AdminTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.BannerException;
import com.motd.be.module.admin.admin.entity.Admin;
import com.motd.be.module.admin.banner.dto.request.BannerSaveRequestForAdmin;
import com.motd.be.module.admin.banner.dto.request.BannerUpdateRequestForAdmin;
import com.motd.be.module.admin.banner.dto.response.BannerAdminFindAllResponseForAdmin;
import com.motd.be.module.admin.banner.dto.response.BannerAdminResponseForAdmin;
import com.motd.be.module.member.banner.entity.Banner;
import com.motd.be.module.member.banner.entity.BannerType;
import com.motd.be.module.member.banner_file.entity.BannerFile;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.shared.aws.enums.S3DirectoryType;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class BannerAdminControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("관리자는 배너를 전체조회 할 수 있다. (삭제된 배너 제외, 정렬 순서 기준)")
	void findAll_withoutDeleted_sortedBySortOrder() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		Banner banner1 = bannerProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 2);
		Banner banner2 = bannerProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 0);
		Banner banner3 = bannerProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1);
		bannerProvider.saveWithIsDeletedTrue(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 3);

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/banners")
					.param(PAGE_STR, "0")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		BannerAdminFindAllResponseForAdmin response = objectMapper.readValue(result.getResponse().getContentAsString(),
			BannerAdminFindAllResponseForAdmin.class);
		List<BannerAdminResponseForAdmin> banners = response.getBanners();

		assertThat(response.getTotalCount()).isEqualTo(3);
		assertThat(banners).hasSize(3);
		assertThat(banners.get(0).getId()).isEqualTo(banner2.getId());
		assertThat(banners.get(1).getId()).isEqualTo(banner3.getId());
		assertThat(banners.get(2).getId()).isEqualTo(banner1.getId());
	}

	@Test
	@DisplayName("관리자는 배너를 전체조회 할 수 있다. (삭제된 배너 포함, 생성순 정렬)")
	void findAll_withDeleted_sortedByCreatedAtDesc() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		Banner banner1 = bannerProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 0);
		Thread.sleep(10);
		Banner banner2 = bannerProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1);
		Thread.sleep(10);
		Banner banner3 = bannerProvider.saveWithIsDeletedTrue(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 2);

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/banners")
					.param(PAGE_STR, "0")
					.param(SHOW_IS_DELETED_STR, Boolean.TRUE.toString())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		BannerAdminFindAllResponseForAdmin response = objectMapper.readValue(result.getResponse().getContentAsString(),
			BannerAdminFindAllResponseForAdmin.class);
		List<BannerAdminResponseForAdmin> banners = response.getBanners();

		assertThat(response.getTotalCount()).isEqualTo(3);
		assertThat(banners).hasSize(3);
		assertThat(banners.get(0).getId()).isEqualTo(banner3.getId());
		assertThat(banners.get(1).getId()).isEqualTo(banner2.getId());
		assertThat(banners.get(2).getId()).isEqualTo(banner1.getId());
	}

	@Test
	@DisplayName("관리자는 배너 전체 조회를 페이징 할 수 있다.")
	void findAll_withPagination() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		for (int i = 0; i < 11; i++) {
			bannerProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), i);
		}

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/banners")
					.param(PAGE_STR, "0")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		BannerAdminFindAllResponseForAdmin response = objectMapper.readValue(result.getResponse().getContentAsString(),
			BannerAdminFindAllResponseForAdmin.class);

		assertThat(response.getTotalCount()).isEqualTo(10);
		assertThat(response.getHasNext()).isTrue();
	}

	@Test
	@DisplayName("관리자는 배너 상세조회가 가능하다. (삭제된 배너 조회)")
	void findDetailIncludingDeleted() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		Banner banner = bannerProvider.saveWithIsDeletedTrue(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 0);

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/banners/{bannerId}", banner.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		BannerAdminResponseForAdmin response = objectMapper.readValue(result.getResponse().getContentAsString(),
			BannerAdminResponseForAdmin.class);

		assertThat(response.getId()).isEqualTo(banner.getId());
	}

	@Test
	@DisplayName("관리자는 배너를 저장할 수 있다. (정렬 순서 조정)")
	void save_reordersSortOrder() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		Banner banner1 = bannerProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 0);
		Banner banner2 = bannerProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1);
		Banner banner3 = bannerProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 2);

		BannerFile thumbnailFile = bannerFileProvider.save(admin, S3DirectoryType.MEMBER_BANNER_THUMBNAIL);
		BannerFile contentFile = bannerFileProvider.save(admin, S3DirectoryType.MEMBER_BANNER_CONTENT);

		BannerSaveRequestForAdmin request = BannerSaveRequestForAdmin.builder()
			.title("new-banner")
			.startAt(formatToDateString(LocalDateTime.now().plusDays(1)))
			.endAt(formatToDateString(LocalDateTime.now().plusDays(3)))
			.sortOrder(1)
			.isWebViewBanner(false)
			.webViewUrl(null)
			.type(BannerType.MEMBER)
			.thumbnailFileId(thumbnailFile.getId())
			.contentFileId(contentFile.getId())
			.build();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/admin/banners")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		List<Banner> banners = bannerProvider.findAll()
			.stream()
			.sorted(Comparator.comparing(Banner::getSortOrder))
			.toList();

		assertThat(banners).hasSize(4);
		assertThat(banners.get(0).getId()).isEqualTo(banner1.getId());
		assertThat(banners.get(2).getId()).isEqualTo(banner2.getId());
		assertThat(banners.get(3).getId()).isEqualTo(banner3.getId());
	}

	@Test
	@DisplayName("관리자는 배너를 저장할 수 있다. (startAt 이 endAt 보다 이후일때 실패한다.)")
	void save_invalidDate() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		BannerFile thumbnailFile = bannerFileProvider.save(admin, S3DirectoryType.MEMBER_BANNER_THUMBNAIL);
		BannerFile contentFile = bannerFileProvider.save(admin, S3DirectoryType.MEMBER_BANNER_CONTENT);

		BannerSaveRequestForAdmin request = BannerSaveRequestForAdmin.builder()
			.title("invalid-date")
			.startAt(formatToDateString(LocalDateTime.now().plusDays(5)))
			.endAt(formatToDateString(LocalDateTime.now().plusDays(1)))
			.sortOrder(0)
			.isWebViewBanner(false)
			.webViewUrl(null)
			.type(BannerType.MEMBER)
			.thumbnailFileId(thumbnailFile.getId())
			.contentFileId(contentFile.getId())
			.build();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/admin/banners")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(BannerException.INVALID_DATE.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(BannerException.INVALID_DATE.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(BannerException.INVALID_DATE.getCode()));
	}

	@Test
	@DisplayName("관리자는 배너를 저장할 수 있다. (썸네일 파일이 없을때)")
	void save_missingThumbnailFile() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		BannerFile contentFile = bannerFileProvider.save(admin, S3DirectoryType.MEMBER_BANNER_CONTENT);

		BannerSaveRequestForAdmin request = BannerSaveRequestForAdmin.builder()
			.title("missing-thumbnail")
			.startAt(formatToDateString(LocalDateTime.now().plusDays(1)))
			.endAt(formatToDateString(LocalDateTime.now().plusDays(3)))
			.sortOrder(0)
			.isWebViewBanner(false)
			.webViewUrl(null)
			.type(BannerType.MEMBER)
			.thumbnailFileId(999999L)
			.contentFileId(contentFile.getId())
			.build();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/admin/banners")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(BannerException.THUMBNAIL_FILE_REQUIRED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(BannerException.THUMBNAIL_FILE_REQUIRED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(BannerException.THUMBNAIL_FILE_REQUIRED.getCode()));
	}

	@Test
	@DisplayName("관리자는 배너를 저장할 수 있다. (웹뷰 배너가 아니면서 컨텐트 파일이 없으면 실패한다.)")
	void save_missingContentFile() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		BannerFile thumbnailFile = bannerFileProvider.save(admin, S3DirectoryType.MEMBER_BANNER_THUMBNAIL);

		BannerSaveRequestForAdmin request = BannerSaveRequestForAdmin.builder()
			.title("missing-content")
			.startAt(formatToDateString(LocalDateTime.now().plusDays(1)))
			.endAt(formatToDateString(LocalDateTime.now().plusDays(3)))
			.sortOrder(0)
			.isWebViewBanner(false)
			.webViewUrl(null)
			.type(BannerType.MEMBER)
			.thumbnailFileId(thumbnailFile.getId())
			.contentFileId(999999L)
			.build();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/admin/banners")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(BannerException.CONTENT_FILE_REQUIRED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(BannerException.CONTENT_FILE_REQUIRED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(BannerException.CONTENT_FILE_REQUIRED.getCode()));
	}

	@Test
	@DisplayName("관리자는 배너를 수정할 수 있다. (정렬 순서 조정)")
	void update_reordersSortOrder() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		BannerFile originThumbnailFile = bannerFileProvider.save(admin, S3DirectoryType.MEMBER_BANNER_THUMBNAIL);

		Banner banner0 = bannerProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 0);
		Banner banner1 = bannerProvider.saveWithFiles(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1,
			originThumbnailFile, null);
		Banner banner2 = bannerProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 2);
		Banner banner3 = bannerProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 3);

		BannerFile thumbnailFile = bannerFileProvider.save(admin, S3DirectoryType.MEMBER_BANNER_THUMBNAIL);
		BannerFile contentFile = bannerFileProvider.save(admin, S3DirectoryType.MEMBER_BANNER_CONTENT);

		BannerUpdateRequestForAdmin request = BannerUpdateRequestForAdmin.builder()
			.title("updated-banner")
			.startAt(formatToDateString(LocalDateTime.now().plusDays(1)))
			.endAt(formatToDateString(LocalDateTime.now().plusDays(3)))
			.sortOrder(3)
			.isWebViewBanner(false)
			.webViewUrl(null)
			.thumbnailFileId(thumbnailFile.getId())
			.contentFileId(contentFile.getId())
			.build();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(
				MockMvcRequestBuilders.put("/api/admin/banners/{bannerId}", banner1.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		List<Banner> banners = bannerProvider.findAll()
			.stream()
			.sorted(Comparator.comparing(Banner::getSortOrder))
			.toList();

		assertThat(banners.get(0).getId()).isEqualTo(banner0.getId());
		assertThat(banners.get(1).getId()).isEqualTo(banner2.getId());
		assertThat(banners.get(2).getId()).isEqualTo(banner3.getId());
		assertThat(banners.get(3).getId()).isEqualTo(banner1.getId());
	}

	@Test
	@DisplayName("관리자는 배너를 수정할 수 있다. (startAt 이 endAt 보다 이후일때 실패한다.)")
	void update_invalidDate() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		Banner banner = bannerProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(2), 0);
		BannerFile thumbnailFile = bannerFileProvider.save(admin, S3DirectoryType.MEMBER_BANNER_THUMBNAIL);
		BannerFile contentFile = bannerFileProvider.save(admin, S3DirectoryType.MEMBER_BANNER_CONTENT);

		BannerUpdateRequestForAdmin request = BannerUpdateRequestForAdmin.builder()
			.title("invalid-update")
			.startAt(formatToDateString(LocalDateTime.now().plusDays(5)))
			.endAt(formatToDateString(LocalDateTime.now().plusDays(1)))
			.sortOrder(0)
			.isWebViewBanner(false)
			.webViewUrl(null)
			.thumbnailFileId(thumbnailFile.getId())
			.contentFileId(contentFile.getId())
			.build();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		mockMvc.perform(
				MockMvcRequestBuilders.put("/api/admin/banners/{bannerId}", banner.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(BannerException.INVALID_DATE.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(BannerException.INVALID_DATE.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(BannerException.INVALID_DATE.getCode()));
	}

	@Test
	@DisplayName("관리자는 배너를 삭제할 수 있다. (정렬 순서 조정)")
	void delete_reordersSortOrder() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		Banner banner1 = bannerProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 0);
		Banner banner2 = bannerProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1);
		Banner banner3 = bannerProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 2);

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/admin/banners/{bannerId}", banner2.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		List<Banner> banners = bannerProvider.findAll()
			.stream()
			.sorted(Comparator.comparing(Banner::getSortOrder))
			.toList();

		Banner deletedBanner = banners.stream()
			.filter(banner -> banner.getId().equals(banner2.getId()))
			.findFirst()
			.orElseThrow();
		assertThat(deletedBanner.getIsDeleted()).isTrue();

		Banner remainedBanner3 = banners.stream()
			.filter(banner -> banner.getId().equals(banner3.getId()))
			.findFirst()
			.orElseThrow();
		assertThat(remainedBanner3.getSortOrder()).isEqualTo(1);
	}

	@Test
	@DisplayName("관리자는 배너를 삭제할 수 있다. (이미지가 존재할때)")
	void delete_withImageSoftDelete() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		BannerFile thumbnailFile = bannerFileProvider.save(admin, S3DirectoryType.MEMBER_BANNER_THUMBNAIL);
		BannerFile contentFile = bannerFileProvider.save(admin, S3DirectoryType.MEMBER_BANNER_CONTENT);

		Banner banner = bannerProvider.saveWithFiles(
			LocalDateTime.now(), LocalDateTime.now().plusDays(5), 0, thumbnailFile, contentFile);

		entityManager.flush();
		entityManager.clear();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/admin/banners/{bannerId}", banner.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// 배너 삭제 확인
		Banner deletedBanner = bannerProvider.findById(banner.getId());
		assertThat(deletedBanner.getIsDeleted()).isTrue();

		// 썸네일 이미지 삭제 확인
		BannerFile deletedThumbnail = bannerFileProvider.findById(thumbnailFile.getId());
		assertThat(deletedThumbnail.getIsDeleted()).isTrue();

		// 컨텐트 이미지 삭제 확인
		BannerFile deletedContent = bannerFileProvider.findById(contentFile.getId());
		assertThat(deletedContent.getIsDeleted()).isTrue();
	}

	@Test
	@DisplayName("관리자는 배너를 수정할 수 있다. (새로운 이미지로 변경할때)")
	void update_withNewImage() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		BannerFile oldThumbnailFile = bannerFileProvider.save(admin, S3DirectoryType.MEMBER_BANNER_THUMBNAIL);
		BannerFile oldContentFile = bannerFileProvider.save(admin, S3DirectoryType.MEMBER_BANNER_CONTENT);
		BannerFile newThumbnailFile = bannerFileProvider.save(admin, S3DirectoryType.MEMBER_BANNER_THUMBNAIL);
		BannerFile newContentFile = bannerFileProvider.save(admin, S3DirectoryType.MEMBER_BANNER_CONTENT);

		Banner banner = bannerProvider.saveWithFiles(
			LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(5), 0, oldThumbnailFile, oldContentFile);

		entityManager.flush();
		entityManager.clear();

		BannerUpdateRequestForAdmin request = BannerUpdateRequestForAdmin.builder()
			.title("updated-banner")
			.startAt(formatToDateString(LocalDateTime.now().minusDays(1)))
			.endAt(formatToDateString(LocalDateTime.now().plusDays(5)))
			.sortOrder(0)
			.isWebViewBanner(false)
			.webViewUrl(null)
			.thumbnailFileId(newThumbnailFile.getId())
			.contentFileId(newContentFile.getId())
			.build();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		mockMvc.perform(
				MockMvcRequestBuilders.put("/api/admin/banners/{bannerId}", banner.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// 새 이미지로 매핑 확인
		Banner updatedBanner = bannerProvider.findById(banner.getId());
		assertThat(updatedBanner.getThumbnailFile().getId()).isEqualTo(newThumbnailFile.getId());
		assertThat(updatedBanner.getContentFile().getId()).isEqualTo(newContentFile.getId());

		// 기존 이미지 소프트 삭제 확인
		BannerFile deletedOldThumbnail = bannerFileProvider.findById(oldThumbnailFile.getId());
		assertThat(deletedOldThumbnail.getIsDeleted()).isTrue();
		BannerFile deletedOldContent = bannerFileProvider.findById(oldContentFile.getId());
		assertThat(deletedOldContent.getIsDeleted()).isTrue();
	}
}
