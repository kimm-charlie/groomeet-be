package com.motd.be.module.director.consulting_sheet.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.ConsultingSheetException;
import com.motd.be.module.director.consulting_sheet.dto.request.ConsultingSheetSaveRequestForDirector;
import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_request.enums.ConsultingRequestStatus;
import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.consulting_sheet.enums.ConsultingSheetStatus;
import com.motd.be.module.member.consulting_sheet_file.entity.ConsultingSheetFile;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class ConsultingSheetControllerForDirectorTest extends BaseIntegrationTest {

	@Test
	@DisplayName("디렉터 컨설팅지 발송이 가능하다 (파일 포함)")
	void 디렉터_컨설팅지_발송이_가능하다_파일_포함() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingRequest consultingRequest = consultingRequestProvider.saveReserved(
			member, directorInfo, LocalDateTime.now());

		ConsultingSheetFile file1 = consultingSheetFileProvider.saveWithoutConsultingSheet(director, 0);
		ConsultingSheetFile file2 = consultingSheetFileProvider.saveWithoutConsultingSheet(director, 1);

		ConsultingSheetSaveRequestForDirector request = ConsultingSheetSaveRequestForDirector.builder()
			.consultingRequestId(consultingRequest.getId())
			.content("컨설팅 내용입니다.")
			.price("50000원")
			.fileIds(List.of(file1.getId(), file2.getId()))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/consulting-sheets")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isCreated());

		// then
		entityManager.flush();
		entityManager.clear();

		ConsultingRequest updatedRequest = entityManager.find(ConsultingRequest.class, consultingRequest.getId());
		assertThat(updatedRequest.getStatus()).isEqualTo(ConsultingRequestStatus.COMPLETED);

		List<ConsultingSheet> sheets = entityManager.createQuery(
				"SELECT cs FROM ConsultingSheet cs WHERE cs.consultingRequest.id = :requestId", ConsultingSheet.class)
			.setParameter("requestId", consultingRequest.getId())
			.getResultList();
		assertThat(sheets).hasSize(1);
		assertThat(sheets.get(0).getStatus()).isEqualTo(ConsultingSheetStatus.PENDING_APPROVAL);
		assertThat(sheets.get(0).getContent()).isEqualTo("컨설팅 내용입니다.");
		assertThat(sheets.get(0).getPrice()).isEqualTo("50000원");
	}

	@Test
	@DisplayName("디렉터 컨설팅지 발송이 가능하다 (파일 없음)")
	void 디렉터_컨설팅지_발송이_가능하다_파일_없음() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingRequest consultingRequest = consultingRequestProvider.saveReserved(
			member, directorInfo, LocalDateTime.now());

		ConsultingSheetSaveRequestForDirector request = ConsultingSheetSaveRequestForDirector.builder()
			.consultingRequestId(consultingRequest.getId())
			.content("파일 없는 컨설팅 내용입니다.")
			.price("30000원")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/consulting-sheets")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isCreated());

		// then
		entityManager.flush();
		entityManager.clear();

		ConsultingRequest updatedRequest = entityManager.find(ConsultingRequest.class, consultingRequest.getId());
		assertThat(updatedRequest.getStatus()).isEqualTo(ConsultingRequestStatus.COMPLETED);
	}

	@Test
	@DisplayName("디렉터 컨설팅지 발송이 실패한다 (선점하지 않은 요청)")
	void 디렉터_컨설팅지_발송이_실패한다_선점하지_않은_요청() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingRequest consultingRequest = consultingRequestProvider.save(member);

		ConsultingSheetSaveRequestForDirector request = ConsultingSheetSaveRequestForDirector.builder()
			.consultingRequestId(consultingRequest.getId())
			.content("컨설팅 내용입니다.")
			.price("50000원")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/consulting-sheets")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_MESSAGE).value(ConsultingSheetException.NOT_RESERVED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ConsultingSheetException.NOT_RESERVED.getCode()));
	}

	@Test
	@DisplayName("디렉터 컨설팅지 발송이 실패한다 (타인 선점 요청)")
	void 디렉터_컨설팅지_발송이_실패한다_타인_선점_요청() throws Exception {
		// given
		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, otherDirectorInfo);

		DirectorInfo myDirectorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member myDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, myDirectorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(myDirector.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingRequest consultingRequest = consultingRequestProvider.saveReserved(
			member, otherDirectorInfo, LocalDateTime.now());

		ConsultingSheetSaveRequestForDirector request = ConsultingSheetSaveRequestForDirector.builder()
			.consultingRequestId(consultingRequest.getId())
			.content("컨설팅 내용입니다.")
			.price("50000원")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/consulting-sheets")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_MESSAGE).value(ConsultingSheetException.NOT_RESERVED_BY_ME.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ConsultingSheetException.NOT_RESERVED_BY_ME.getCode()));
	}

	@Test
	@DisplayName("디렉터 컨설팅지 발송이 실패한다 (이미 완료된 요청)")
	void 디렉터_컨설팅지_발송이_실패한다_이미_완료된_요청() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingRequest consultingRequest = consultingRequestProvider.saveCompleted(member);

		ConsultingSheetSaveRequestForDirector request = ConsultingSheetSaveRequestForDirector.builder()
			.consultingRequestId(consultingRequest.getId())
			.content("컨설팅 내용입니다.")
			.price("50000원")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/consulting-sheets")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_MESSAGE).value(ConsultingSheetException.ALREADY_COMPLETED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ConsultingSheetException.ALREADY_COMPLETED.getCode()));
	}

	@Test
	@DisplayName("디렉터 컨설팅지 발송이 실패한다 (파일 소유권 위반)")
	void 디렉터_컨설팅지_발송이_실패한다_파일_소유권_위반() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingRequest consultingRequest = consultingRequestProvider.saveReserved(
			member, directorInfo, LocalDateTime.now());

		Member otherMember = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingSheetFile otherFile = consultingSheetFileProvider.saveWithoutConsultingSheet(otherMember, 0);

		ConsultingSheetSaveRequestForDirector request = ConsultingSheetSaveRequestForDirector.builder()
			.consultingRequestId(consultingRequest.getId())
			.content("컨설팅 내용입니다.")
			.price("50000원")
			.fileIds(List.of(otherFile.getId()))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/consulting-sheets")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_MESSAGE).value(ConsultingSheetException.FILE_NOT_OWNED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ConsultingSheetException.FILE_NOT_OWNED.getCode()));
	}

	@Test
	@DisplayName("디렉터 컨설팅지 발송이 실패한다 (이미 매핑된 파일)")
	void 디렉터_컨설팅지_발송이_실패한다_이미_매핑된_파일() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(
			INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		ConsultingRequest consultingRequest = consultingRequestProvider.saveReserved(
			member, directorInfo, LocalDateTime.now());

		ConsultingRequest otherRequest = consultingRequestProvider.saveReserved(
			memberProvider.saveMember(SignInPlatform.KAKAO), directorInfo, LocalDateTime.now());
		ConsultingSheet existingSheet = consultingSheetProvider.savePendingApproval(otherRequest, directorInfo);
		ConsultingSheetFile mappedFile = consultingSheetFileProvider.save(existingSheet, director, 0);

		ConsultingSheetSaveRequestForDirector request = ConsultingSheetSaveRequestForDirector.builder()
			.consultingRequestId(consultingRequest.getId())
			.content("컨설팅 내용입니다.")
			.price("50000원")
			.fileIds(List.of(mappedFile.getId()))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/consulting-sheets")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_MESSAGE).value(ConsultingSheetException.FILE_ALREADY_MAPPED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ConsultingSheetException.FILE_ALREADY_MAPPED.getCode()));
	}
}
