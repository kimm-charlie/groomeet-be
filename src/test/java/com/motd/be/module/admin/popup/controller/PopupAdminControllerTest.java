package com.motd.be.module.admin.popup.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;
import static com.motd.be.provider.module.admin.AdminTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.PopUpException;
import com.motd.be.module.admin.admin.entity.Admin;
import com.motd.be.module.admin.popup.dto.request.PopupSaveRequestForAdmin;
import com.motd.be.module.admin.popup.dto.request.PopupUpdateRequestForAdmin;
import com.motd.be.module.admin.popup.dto.response.PopupAdminFindAllResponseForAdmin;
import com.motd.be.module.admin.popup.dto.response.PopupAdminResponseForAdmin;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.popup.entity.Popup;
import com.motd.be.module.member.popup.entity.PopupType;
import com.motd.be.module.member.popup_file.entity.PopupFile;
import com.motd.be.shared.aws.enums.S3DirectoryType;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class PopupAdminControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("관리자는 팝업 전체 조회가 가능하다. (삭제되지 않았으면서, expired되지 않은 팝업만 조회)")
	void findAll_showIsDeletedFalse() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		Popup active = popupAdminProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(5), Boolean.FALSE, 0);
		Popup expired = popupAdminProvider.save(LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1),
			Boolean.FALSE, 1);
		popupAdminProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(5), Boolean.TRUE, 2);

		entityManager.flush();
		entityManager.clear();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/popups")
					.param(SHOW_IS_DELETED_STR, Boolean.FALSE.toString())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		PopupAdminFindAllResponseForAdmin response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PopupAdminFindAllResponseForAdmin.class);
		List<PopupAdminResponseForAdmin> popups = response.getPopups();

		assertThat(popups).hasSize(1);
		assertThat(popups).extracting(PopupAdminResponseForAdmin::getId)
			.containsExactlyInAnyOrder(active.getId());
	}

	@Test
	@DisplayName("관리자는 팝업 전체 조회가 가능하다. (삭제/종료된 팝업을 포함하여)")
	void findAll_showIsDeletedTrue() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		Popup active = popupAdminProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(5), Boolean.FALSE, 0);
		Popup expired = popupAdminProvider.save(LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1),
			Boolean.FALSE, 1);
		Popup deleted = popupAdminProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(5), Boolean.TRUE, 2);

		entityManager.flush();
		entityManager.clear();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/popups")
					.param(SHOW_IS_DELETED_STR, Boolean.TRUE.toString())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		PopupAdminFindAllResponseForAdmin response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PopupAdminFindAllResponseForAdmin.class);
		List<PopupAdminResponseForAdmin> popups = response.getPopups();

		assertThat(popups).hasSize(3);
		assertThat(popups).extracting(PopupAdminResponseForAdmin::getId)
			.containsExactlyInAnyOrder(active.getId(), expired.getId(), deleted.getId());
	}

	@Test
	@DisplayName("관리자는 팝업 상세 조회가 가능하다.")
	void findDetail() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Popup popup = popupAdminProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(5), Boolean.FALSE, 0);

		entityManager.flush();
		entityManager.clear();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/popups/{popupId}", popup.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		JSONObject response = new JSONObject(result.getResponse().getContentAsString());
		assertThat(response.getLong(ID_STR)).isEqualTo(popup.getId());
		assertThat(response.getString("title")).isEqualTo(popup.getTitle());
	}

	@Test
	@DisplayName("관리자는 팝업을 저장할 수 있다. (컨텐츠 파일 없음)")
	void save_withoutContentFile() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		PopupFile thumbnailFile = popupFileProvider.save(admin, S3DirectoryType.MEMBER_POPUP_THUMBNAIL);

		PopupSaveRequestForAdmin request = PopupSaveRequestForAdmin.builder()
			.title(TITLE_STR)
			.startAt(formatToDateString(LocalDateTime.now().plusDays(1)))
			.endAt(formatToDateString(LocalDateTime.now().plusDays(5)))
			.sortOrder(0)
			.thumbnailFileId(thumbnailFile.getId())
			.type(PopupType.MEMBER)
			.build();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/admin/popups")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		List<Popup> popups = popupAdminProvider.findAll();
		assertThat(popups).hasSize(1);
	}

	@Test
	@DisplayName("관리자는 팝업을 저장할 수 있다. (유효하지 않은 시작일로 저장시)")
	void save_invalidDate() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		PopupFile thumbnailFile = popupFileProvider.save(admin, S3DirectoryType.MEMBER_POPUP_THUMBNAIL);

		PopupSaveRequestForAdmin request = PopupSaveRequestForAdmin.builder()
			.title(TITLE_STR)
			.startAt(formatToDateString(LocalDateTime.now().plusDays(5)))
			.endAt(formatToDateString(LocalDateTime.now().plusDays(1)))
			.sortOrder(0)
			.thumbnailFileId(thumbnailFile.getId())
			.type(PopupType.MEMBER)
			.build();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/admin/popups")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(PopUpException.INVALID_DATE.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(PopUpException.INVALID_DATE.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(PopUpException.INVALID_DATE.getCode()));
	}

	@Test
	@DisplayName("관리자는 팝업을 수정할 수 있다. (시작일이 과거여도 허용)")
	void update_allowPastStartAt() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Popup popup = popupAdminProvider.save(LocalDateTime.now().minusDays(3), LocalDateTime.now().plusDays(3),
			Boolean.FALSE, 0);

		PopupFile thumbnailFile = popupFileProvider.save(admin, S3DirectoryType.MEMBER_POPUP_THUMBNAIL);

		PopupUpdateRequestForAdmin request = PopupUpdateRequestForAdmin.builder()
			.title(UPDATED_TITLE_STR)
			.startAt(formatToDateString(LocalDateTime.now().minusDays(1)))
			.endAt(formatToDateString(LocalDateTime.now().plusDays(5)))
			.sortOrder(0)
			.thumbnailFileId(thumbnailFile.getId())
			.linkUrl(LINK_URL)
			.build();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		mockMvc.perform(
				MockMvcRequestBuilders.put("/api/admin/popups/{popupId}", popup.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		Popup updated = popupAdminProvider.findById(popup.getId());
		assertThat(updated.getTitle()).isEqualTo(UPDATED_TITLE_STR);
	}

	@Test
	@DisplayName("관리자는 팝업을 수정할 수 있다.(유효하지 않은 날짜로 수정 시)")
	void update_invalidDate() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Popup popup = popupAdminProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(3), Boolean.FALSE, 0);

		PopupFile thumbnailFile = popupFileProvider.save(admin, S3DirectoryType.MEMBER_POPUP_THUMBNAIL);

		PopupUpdateRequestForAdmin request = PopupUpdateRequestForAdmin.builder()
			.title(UPDATED_TITLE_STR)
			.startAt(formatToDateString(LocalDateTime.now().plusDays(5)))
			.endAt(formatToDateString(LocalDateTime.now().plusDays(1)))
			.sortOrder(0)
			.thumbnailFileId(thumbnailFile.getId())
			.build();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		mockMvc.perform(
				MockMvcRequestBuilders.put("/api/admin/popups/{popupId}", popup.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(PopUpException.INVALID_DATE.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(PopUpException.INVALID_DATE.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(PopUpException.INVALID_DATE.getCode()));
	}

	@Test
	@DisplayName("관리자가 팝업을 삭제하면 이후 팝업들의 sortOrder가 당겨진다.")
	void delete_reordersSortOrder() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		PopupFile popupFile = popupFileProvider.save(admin, S3DirectoryType.MEMBER_POPUP_THUMBNAIL);

		Popup popup1 = popupAdminProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(5), Boolean.FALSE, 0);
		Popup popup2 = popupAdminProvider.saveWithThumbnail(LocalDateTime.now(), LocalDateTime.now().plusDays(5),
			Boolean.FALSE, 1, popupFile);
		Popup popup3 = popupAdminProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(5), Boolean.FALSE, 2);

		entityManager.flush();
		entityManager.clear();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/admin/popups/{popupId}", popup2.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		List<Popup> popups = popupAdminProvider.findAll()
			.stream()
			.sorted(Comparator.comparing(Popup::getSortOrder))
			.toList();

		Popup deletedPopup = popups.stream()
			.filter(p -> p.getId().equals(popup2.getId()))
			.findFirst()
			.orElseThrow();
		assertThat(deletedPopup.getIsDeleted()).isTrue();

		Popup remainedPopup3 = popups.stream()
			.filter(p -> p.getId().equals(popup3.getId()))
			.findFirst()
			.orElseThrow();
		assertThat(remainedPopup3.getSortOrder()).isEqualTo(1);
	}

	@Test
	@DisplayName("관리자가 팝업을 삭제하면 관련 이미지도 isDeleted가 true가 된다.")
	void delete_withImageSoftDelete() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		PopupFile thumbnailFile = popupFileProvider.save(admin, S3DirectoryType.MEMBER_POPUP_THUMBNAIL);

		Popup popup = popupAdminProvider.saveWithThumbnail(
			LocalDateTime.now(), LocalDateTime.now().plusDays(5), Boolean.FALSE, 0, thumbnailFile);

		entityManager.flush();
		entityManager.clear();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/admin/popups/{popupId}", popup.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// 팝업 삭제 확인
		Popup deletedPopup = popupAdminProvider.findById(popup.getId());
		assertThat(deletedPopup.getIsDeleted()).isTrue();

		// 이미지 삭제 확인
		PopupFile deletedThumbnail = popupFileProvider.findById(thumbnailFile.getId());
		assertThat(deletedThumbnail.getIsDeleted()).isTrue();
	}

	@Test
	@DisplayName("관리자는 팝업을 수정할 수 있다. (새 이미지로 변경 시 기존 이미지는 삭제 처리된다.)")
	void update_withNewImage() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		PopupFile oldThumbnailFile = popupFileProvider.save(admin, S3DirectoryType.MEMBER_POPUP_THUMBNAIL);
		PopupFile newThumbnailFile = popupFileProvider.save(admin, S3DirectoryType.MEMBER_POPUP_THUMBNAIL);

		Popup popup = popupAdminProvider.saveWithThumbnail(
			LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(5), Boolean.FALSE, 0, oldThumbnailFile);

		entityManager.flush();
		entityManager.clear();

		PopupUpdateRequestForAdmin request = PopupUpdateRequestForAdmin.builder()
			.title(UPDATED_TITLE_STR)
			.startAt(formatToDateString(LocalDateTime.now().minusDays(1)))
			.endAt(formatToDateString(LocalDateTime.now().plusDays(5)))
			.sortOrder(0)
			.thumbnailFileId(newThumbnailFile.getId())
			.linkUrl(LINK_URL)
			.build();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		mockMvc.perform(
				MockMvcRequestBuilders.put("/api/admin/popups/{popupId}", popup.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// 새 이미지로 매핑 확인
		Popup updatedPopup = popupAdminProvider.findById(popup.getId());
		assertThat(updatedPopup.getThumbnailFile().getId()).isEqualTo(newThumbnailFile.getId());

		// 기존 이미지 삭제 확인
		PopupFile deletedOldThumbnail = popupFileProvider.findById(oldThumbnailFile.getId());
		assertThat(deletedOldThumbnail.getIsDeleted()).isTrue();
	}
}
