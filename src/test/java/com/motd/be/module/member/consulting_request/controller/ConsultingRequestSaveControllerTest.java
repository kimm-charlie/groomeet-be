package com.motd.be.module.member.consulting_request.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.ValidationMessages.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.ConsultingRequestException;
import com.motd.be.exception.exceptions.LocationException;
import com.motd.be.module.member.consulting_request.dto.request.ConsultingImageFileRequest;
import com.motd.be.module.member.consulting_request.dto.request.ConsultingRequestSaveRequest;
import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_request.enums.ConsultingRequestStatus;
import com.motd.be.module.member.consulting_request_file.entity.ConsultingRequestFile;
import com.motd.be.module.member.consulting_request_file.enums.ConsultingRequestImageCategory;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class ConsultingRequestSaveControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("컨설팅 요청이 가능하다")
	void 컨설팅_요청이_가능하다() throws Exception {
		// given
		Member inviter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		codeUsageHistoryProvider.saveWithInviterAndInvitee(inviter, member);

		Location location = locationProvider.save("서울", LocationType.CITY);

		List<ConsultingImageFileRequest> files = new ArrayList<>();
		for (ConsultingRequestImageCategory category : List.of(
			ConsultingRequestImageCategory.FRONT,
			ConsultingRequestImageCategory.SIDE,
			ConsultingRequestImageCategory.TOP,
			ConsultingRequestImageCategory.ASPIRATION)) {
			ConsultingRequestFile file = consultingRequestFileProvider.saveWithoutConsultingRequest(member);
			files.add(ConsultingImageFileRequest.builder()
				.fileId(file.getId())
				.category(category)
				.build());
		}

		ConsultingRequestSaveRequest request = ConsultingRequestSaveRequest.builder()
			.usesHairProduct(true)
			.prefersExposedForehead(false)
			.recentProcedure("없음")
			.locations(List.of(location.getId()))
			.files(files)
			.build();

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/consulting-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		// then
		List<ConsultingRequest> consultingRequests = consultingRequestProvider.findAll();
		assertThat(consultingRequests).hasSize(1);

		ConsultingRequest saved = consultingRequests.get(0);
		assertThat(saved.getMember().getId()).isEqualTo(member.getId());
		assertThat(saved.getUsesHairProduct()).isTrue();
		assertThat(saved.getPrefersExposedForehead()).isFalse();
		assertThat(saved.getRecentProcedure()).isEqualTo("없음");
		assertThat(saved.getStatus()).isEqualTo(ConsultingRequestStatus.PENDING);

		List<ConsultingRequestFile> savedFiles = consultingRequestFileProvider.findAll();
		assertThat(savedFiles).allSatisfy(file -> {
			assertThat(file.getConsultingRequest()).isNotNull();
			assertThat(file.getImageCategory()).isNotNull();
		});
	}

	@Test
	@DisplayName("컨설팅 요청이 ASPIRATION 없이도 가능하다")
	void 컨설팅_요청이_ASPIRATION_없이도_가능하다() throws Exception {
		// given
		Member inviter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		codeUsageHistoryProvider.saveWithInviterAndInvitee(inviter, member);

		Location location = locationProvider.save("서울", LocationType.CITY);

		List<ConsultingImageFileRequest> files = new ArrayList<>();
		for (ConsultingRequestImageCategory category : List.of(
			ConsultingRequestImageCategory.FRONT,
			ConsultingRequestImageCategory.SIDE,
			ConsultingRequestImageCategory.TOP)) {
			ConsultingRequestFile file = consultingRequestFileProvider.saveWithoutConsultingRequest(member);
			files.add(ConsultingImageFileRequest.builder()
				.fileId(file.getId())
				.category(category)
				.build());
		}

		ConsultingRequestSaveRequest request = ConsultingRequestSaveRequest.builder()
			.usesHairProduct(true)
			.prefersExposedForehead(false)
			.recentProcedure("3개월 전 펌")
			.locations(List.of(location.getId()))
			.files(files)
			.build();

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/consulting-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		// then
		List<ConsultingRequest> consultingRequests = consultingRequestProvider.findAll();
		assertThat(consultingRequests).hasSize(1);

		ConsultingRequest saved = consultingRequests.get(0);
		assertThat(saved.getRecentProcedure()).isEqualTo("3개월 전 펌");
	}

	@Test
	@DisplayName("컨설팅 요청 실패 - 이미 요청이 존재하는 경우")
	void 컨설팅_요청_실패_이미_요청이_존재하는_경우() throws Exception {
		// given
		Member inviter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		codeUsageHistoryProvider.saveWithInviterAndInvitee(inviter, member);
		consultingRequestProvider.save(member);

		Location location = locationProvider.save("서울", LocationType.CITY);

		List<ConsultingImageFileRequest> files = new ArrayList<>();
		for (ConsultingRequestImageCategory category : List.of(
			ConsultingRequestImageCategory.FRONT,
			ConsultingRequestImageCategory.SIDE,
			ConsultingRequestImageCategory.TOP,
			ConsultingRequestImageCategory.ASPIRATION)) {
			ConsultingRequestFile file = consultingRequestFileProvider.saveWithoutConsultingRequest(member);
			files.add(ConsultingImageFileRequest.builder()
				.fileId(file.getId())
				.category(category)
				.build());
		}

		ConsultingRequestSaveRequest request = ConsultingRequestSaveRequest.builder()
			.usesHairProduct(true)
			.prefersExposedForehead(false)
			.recentProcedure("없음")
			.locations(List.of(location.getId()))
			.files(files)
			.build();

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/consulting-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath(ERROR_MESSAGE).value(ConsultingRequestException.ALREADY_EXISTS.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ConsultingRequestException.ALREADY_EXISTS.getCode()));
	}

	@Test
	@DisplayName("컨설팅 요청 실패 - 자격이 없는 경우")
	void 컨설팅_요청_실패_자격이_없는_경우() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Location location = locationProvider.save("서울", LocationType.CITY);

		List<ConsultingImageFileRequest> files = new ArrayList<>();
		for (ConsultingRequestImageCategory category : List.of(
			ConsultingRequestImageCategory.FRONT,
			ConsultingRequestImageCategory.SIDE,
			ConsultingRequestImageCategory.TOP,
			ConsultingRequestImageCategory.ASPIRATION)) {
			ConsultingRequestFile file = consultingRequestFileProvider.saveWithoutConsultingRequest(member);
			files.add(ConsultingImageFileRequest.builder()
				.fileId(file.getId())
				.category(category)
				.build());
		}

		ConsultingRequestSaveRequest request = ConsultingRequestSaveRequest.builder()
			.usesHairProduct(true)
			.prefersExposedForehead(false)
			.recentProcedure("없음")
			.locations(List.of(location.getId()))
			.files(files)
			.build();

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/consulting-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_MESSAGE).value(ConsultingRequestException.NOT_ELIGIBLE.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ConsultingRequestException.NOT_ELIGIBLE.getCode()));
	}

	@Test
	@DisplayName("컨설팅 요청 실패 - 타인의 파일 ID 사용")
	void 컨설팅_요청_실패_타인의_파일_ID_사용() throws Exception {
		// given
		Member inviter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member otherMember = memberProvider.saveMember(SignInPlatform.KAKAO);
		codeUsageHistoryProvider.saveWithInviterAndInvitee(inviter, member);

		Location location = locationProvider.save("서울", LocationType.CITY);

		List<ConsultingImageFileRequest> files = new ArrayList<>();
		for (ConsultingRequestImageCategory category : List.of(
			ConsultingRequestImageCategory.FRONT,
			ConsultingRequestImageCategory.SIDE,
			ConsultingRequestImageCategory.TOP,
			ConsultingRequestImageCategory.ASPIRATION)) {
			ConsultingRequestFile file = consultingRequestFileProvider.saveWithoutConsultingRequest(otherMember);
			files.add(ConsultingImageFileRequest.builder()
				.fileId(file.getId())
				.category(category)
				.build());
		}

		ConsultingRequestSaveRequest request = ConsultingRequestSaveRequest.builder()
			.usesHairProduct(true)
			.prefersExposedForehead(false)
			.recentProcedure("없음")
			.locations(List.of(location.getId()))
			.files(files)
			.build();

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/consulting-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_MESSAGE).value(ConsultingRequestException.FILE_NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ConsultingRequestException.FILE_NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("컨설팅 요청 실패 - 카테고리별 이미지 개수 초과")
	void 컨설팅_요청_실패_카테고리별_이미지_개수_초과() throws Exception {
		// given
		Member inviter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		codeUsageHistoryProvider.saveWithInviterAndInvitee(inviter, member);

		Location location = locationProvider.save("서울", LocationType.CITY);

		List<ConsultingImageFileRequest> files = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			ConsultingRequestFile file = consultingRequestFileProvider.saveWithoutConsultingRequest(member);
			files.add(ConsultingImageFileRequest.builder()
				.fileId(file.getId())
				.category(ConsultingRequestImageCategory.FRONT)
				.build());
		}
		for (ConsultingRequestImageCategory category : List.of(
			ConsultingRequestImageCategory.SIDE,
			ConsultingRequestImageCategory.TOP,
			ConsultingRequestImageCategory.ASPIRATION)) {
			ConsultingRequestFile file = consultingRequestFileProvider.saveWithoutConsultingRequest(member);
			files.add(ConsultingImageFileRequest.builder()
				.fileId(file.getId())
				.category(category)
				.build());
		}

		ConsultingRequestSaveRequest request = ConsultingRequestSaveRequest.builder()
			.usesHairProduct(true)
			.prefersExposedForehead(false)
			.recentProcedure("없음")
			.locations(List.of(location.getId()))
			.files(files)
			.build();

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/consulting-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_MESSAGE).value(ConsultingRequestException.INVALID_FILE_CATEGORY_COUNT.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ConsultingRequestException.INVALID_FILE_CATEGORY_COUNT.getCode()));
	}

	@Test
	@DisplayName("컨설팅 요청 실패 - 파일 ID가 null인 경우")
	void 컨설팅_요청_실패_파일_ID가_null인_경우() throws Exception {
		// given
		Member inviter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		codeUsageHistoryProvider.saveWithInviterAndInvitee(inviter, member);

		Location location = locationProvider.save("서울", LocationType.CITY);

		List<ConsultingImageFileRequest> files = new ArrayList<>();
		files.add(ConsultingImageFileRequest.builder()
			.fileId(null)
			.category(ConsultingRequestImageCategory.FRONT)
			.build());
		for (ConsultingRequestImageCategory category : List.of(
			ConsultingRequestImageCategory.SIDE,
			ConsultingRequestImageCategory.TOP,
			ConsultingRequestImageCategory.ASPIRATION)) {
			ConsultingRequestFile file = consultingRequestFileProvider.saveWithoutConsultingRequest(member);
			files.add(ConsultingImageFileRequest.builder()
				.fileId(file.getId())
				.category(category)
				.build());
		}

		ConsultingRequestSaveRequest request = ConsultingRequestSaveRequest.builder()
			.usesHairProduct(true)
			.prefersExposedForehead(false)
			.recentProcedure("없음")
			.locations(List.of(location.getId()))
			.files(files)
			.build();

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/consulting-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_MESSAGE).value(ConsultingRequestException.INVALID_FILE_REQUEST.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ConsultingRequestException.INVALID_FILE_REQUEST.getCode()));
	}

	@Test
	@DisplayName("컨설팅 요청 실패 - 이미지 카테고리가 null인 경우")
	void 컨설팅_요청_실패_이미지_카테고리가_null인_경우() throws Exception {
		// given
		Member inviter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		codeUsageHistoryProvider.saveWithInviterAndInvitee(inviter, member);

		Location location = locationProvider.save("서울", LocationType.CITY);

		List<ConsultingImageFileRequest> files = new ArrayList<>();
		ConsultingRequestFile file = consultingRequestFileProvider.saveWithoutConsultingRequest(member);
		files.add(ConsultingImageFileRequest.builder()
			.fileId(file.getId())
			.category(null)
			.build());
		for (ConsultingRequestImageCategory category : List.of(
			ConsultingRequestImageCategory.SIDE,
			ConsultingRequestImageCategory.TOP,
			ConsultingRequestImageCategory.ASPIRATION)) {
			ConsultingRequestFile otherFile = consultingRequestFileProvider.saveWithoutConsultingRequest(member);
			files.add(ConsultingImageFileRequest.builder()
				.fileId(otherFile.getId())
				.category(category)
				.build());
		}

		ConsultingRequestSaveRequest request = ConsultingRequestSaveRequest.builder()
			.usesHairProduct(true)
			.prefersExposedForehead(false)
			.recentProcedure("없음")
			.locations(List.of(location.getId()))
			.files(files)
			.build();

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/consulting-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_MESSAGE).value(ConsultingRequestException.INVALID_FILE_REQUEST.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ConsultingRequestException.INVALID_FILE_REQUEST.getCode()));
	}

	@Test
	@DisplayName("컨설팅 요청 실패 - files 배열에 null 원소가 포함된 경우")
	void 컨설팅_요청_실패_files_배열에_null_원소가_포함된_경우() throws Exception {
		// given
		Member inviter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		codeUsageHistoryProvider.saveWithInviterAndInvitee(inviter, member);

		Location location = locationProvider.save("서울", LocationType.CITY);

		String json = objectMapper.writeValueAsString(ConsultingRequestSaveRequest.builder()
			.usesHairProduct(true)
			.prefersExposedForehead(false)
			.recentProcedure("없음")
			.locations(List.of(location.getId()))
			.files(List.of(
				ConsultingImageFileRequest.builder().fileId(1L).category(ConsultingRequestImageCategory.FRONT).build()
			))
			.build());
		// files 배열에 null 원소를 수동 삽입
		json = json.replace("[{", "[null,{");

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/consulting-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_MESSAGE).value(ConsultingRequestException.INVALID_FILE_REQUEST.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ConsultingRequestException.INVALID_FILE_REQUEST.getCode()));
	}

	@Test
	@DisplayName("컨설팅 요청 실패 - 중복 파일 ID 사용")
	void 컨설팅_요청_실패_중복_파일_ID_사용() throws Exception {
		// given
		Member inviter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		codeUsageHistoryProvider.saveWithInviterAndInvitee(inviter, member);

		Location location = locationProvider.save("서울", LocationType.CITY);

		ConsultingRequestFile file = consultingRequestFileProvider.saveWithoutConsultingRequest(member);
		List<ConsultingImageFileRequest> files = new ArrayList<>();
		files.add(ConsultingImageFileRequest.builder()
			.fileId(file.getId())
			.category(ConsultingRequestImageCategory.FRONT)
			.build());
		files.add(ConsultingImageFileRequest.builder()
			.fileId(file.getId())
			.category(ConsultingRequestImageCategory.SIDE)
			.build());
		for (ConsultingRequestImageCategory category : List.of(
			ConsultingRequestImageCategory.TOP,
			ConsultingRequestImageCategory.ASPIRATION)) {
			ConsultingRequestFile otherFile = consultingRequestFileProvider.saveWithoutConsultingRequest(member);
			files.add(ConsultingImageFileRequest.builder()
				.fileId(otherFile.getId())
				.category(category)
				.build());
		}

		ConsultingRequestSaveRequest request = ConsultingRequestSaveRequest.builder()
			.usesHairProduct(true)
			.prefersExposedForehead(false)
			.recentProcedure("없음")
			.locations(List.of(location.getId()))
			.files(files)
			.build();

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/consulting-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_MESSAGE).value(ConsultingRequestException.INVALID_FILE_REQUEST.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ConsultingRequestException.INVALID_FILE_REQUEST.getCode()));
	}

	@Test
	@DisplayName("컨설팅 요청 실패 - 최근 시술 여부 길이 초과")
	void 컨설팅_요청_실패_최근_시술_여부_길이_초과() throws Exception {
		// given
		Member inviter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		codeUsageHistoryProvider.saveWithInviterAndInvitee(inviter, member);

		Location location = locationProvider.save("서울", LocationType.CITY);

		List<ConsultingImageFileRequest> files = new ArrayList<>();
		for (ConsultingRequestImageCategory category : List.of(
			ConsultingRequestImageCategory.FRONT,
			ConsultingRequestImageCategory.SIDE,
			ConsultingRequestImageCategory.TOP)) {
			ConsultingRequestFile file = consultingRequestFileProvider.saveWithoutConsultingRequest(member);
			files.add(ConsultingImageFileRequest.builder()
				.fileId(file.getId())
				.category(category)
				.build());
		}

		ConsultingRequestSaveRequest request = ConsultingRequestSaveRequest.builder()
			.usesHairProduct(true)
			.prefersExposedForehead(false)
			.recentProcedure("가".repeat(51))
			.locations(List.of(location.getId()))
			.files(files)
			.build();

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/consulting-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_MESSAGE).value(CONSULTING_RECENT_PROCEDURE_MAX_LENGTH));
	}

	@Test
	@DisplayName("컨설팅 요청 실패 - 존재하지 않는 지역 ID")
	void 컨설팅_요청_실패_존재하지_않는_지역_ID() throws Exception {
		// given
		Member inviter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		codeUsageHistoryProvider.saveWithInviterAndInvitee(inviter, member);

		List<ConsultingImageFileRequest> files = new ArrayList<>();
		for (ConsultingRequestImageCategory category : List.of(
			ConsultingRequestImageCategory.FRONT,
			ConsultingRequestImageCategory.SIDE,
			ConsultingRequestImageCategory.TOP)) {
			ConsultingRequestFile file = consultingRequestFileProvider.saveWithoutConsultingRequest(member);
			files.add(ConsultingImageFileRequest.builder()
				.fileId(file.getId())
				.category(category)
				.build());
		}

		ConsultingRequestSaveRequest request = ConsultingRequestSaveRequest.builder()
			.usesHairProduct(true)
			.prefersExposedForehead(false)
			.recentProcedure("없음")
			.locations(List.of(99999L))
			.files(files)
			.build();

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/consulting-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_MESSAGE).value(LocationException.INVALID_LOCATION_EXIST.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(LocationException.INVALID_LOCATION_EXIST.getCode()));
	}
}
