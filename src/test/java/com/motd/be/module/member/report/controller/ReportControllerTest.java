package com.motd.be.module.member.report.controller;

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
import com.motd.be.exception.exceptions.MemberBlockException;
import com.motd.be.exception.exceptions.MemberException;
import com.motd.be.exception.exceptions.MemberReportException;
import com.motd.be.exception.exceptions.ReportFileException;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.member_block.entity.MemberBlock;
import com.motd.be.module.member.member_director_favorite.entity.MemberDirectorFavorite;
import com.motd.be.module.member.report.dto.request.ReportRequest;
import com.motd.be.module.member.report.entity.Report;
import com.motd.be.module.member.report.entity.ReportReason;
import com.motd.be.module.member.report.entity.ReportType;
import com.motd.be.module.member.report_file.entity.ReportFile;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class ReportControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("회원은 디렉터를 신고할 수 있다. (정상 케이스)")
	void save_success() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member reporter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(reporter.getId());

		ReportRequest request = ReportRequest.builder()
			.reportedId(director.getId())
			.reason(ReportReason.불법_광고_홍보.name())
			.reportType(ReportType.CHAT_ROOM.name())
			.description(DESCRIPTION_STR)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/reports")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		// then
		List<Report> reports = memberReportProvider.findAll();
		assertThat(reports).hasSize(1);
		assertThat(reports.get(0).getReporter().getId()).isEqualTo(reporter.getId());
		assertThat(reports.get(0).getReported().getId()).isEqualTo(director.getId());

		List<MemberBlock> blocks = memberBlockProvider.findAll();
		assertThat(blocks).hasSize(1);
		assertThat(blocks.get(0).getBlocker().getId()).isEqualTo(reporter.getId());
		assertThat(blocks.get(0).getBlocked().getId()).isEqualTo(director.getId());
	}

	@Test
	@DisplayName("회원은 디렉터를 신고할 수 있다. (이미지 존재 케이스)")
	void save_withImages_success() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member reporter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(reporter.getId());

		ReportFile image1 = reportFileProvider.save(reporter);
		ReportFile image2 = reportFileProvider.save(reporter);

		ReportRequest request = ReportRequest.builder()
			.reportedId(director.getId())
			.reason(ReportReason.불법_광고_홍보.name())
			.reportType(ReportType.CHAT_ROOM.name())
			.imageIds(List.of(image1.getId(), image2.getId()))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/reports")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		// then
		List<Report> reports = memberReportProvider.findAll();
		assertThat(reports).hasSize(1);
		Report savedReport = reports.get(0);

		List<ReportFile> mappedImages = reportFileRepository.findAll();
		assertThat(mappedImages).hasSize(2);
		mappedImages.forEach(image -> {
			assertThat(image.getReport().getId()).isEqualTo(savedReport.getId());
			assertThat(image.getIsDeleted()).isFalse();
		});
	}

	@Test
	@DisplayName("회원은 디렉터를 신고할 수 있다. (이미지가 신고자의 이미지가 아닐경우)")
	void save_whenImageNotOwnedByReporter_throwsException() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member reporter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(reporter.getId());

		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);
		ReportFile image = reportFileProvider.save(otherMember);

		ReportRequest request = ReportRequest.builder()
			.reportedId(director.getId())
			.reason(ReportReason.불법_광고_홍보.name())
			.reportType(ReportType.CHAT_ROOM.name())
			.imageIds(List.of(image.getId()))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/reports")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath(ERROR_STATUS).value(ReportFileException.NOT_OWNED_BY.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ReportFileException.NOT_OWNED_BY.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ReportFileException.NOT_OWNED_BY.getCode()));
	}

	@Test
	@DisplayName("회원은 디렉터를 신고할 수 있다.(즐겨찾기가 해제되는 케이스)")
	void save_whenFavoriteExists_removeFavorite() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member reporter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(reporter.getId());

		// 즐겨찾기 설정
		memberDirectorFavoriteProvider.save(reporter, director);

		ReportRequest request = ReportRequest.builder()
			.reportedId(director.getId())
			.reason(ReportReason.불법_광고_홍보.name())
			.reportType(ReportType.CHAT_ROOM.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/reports")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		// then
		List<MemberDirectorFavorite> favorites = memberDirectorFavoriteProvider.findAll();
		assertThat(favorites).isEmpty();
	}

	@Test
	@DisplayName("회원은 디렉터를 신고할 수 있다.(채팅방이 존재될떄)")
	void save_whenChatRoomExists_leaveChatRoom() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member reporter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(reporter.getId());

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, reporter);

		ReportRequest request = ReportRequest.builder()
			.reportedId(director.getId())
			.reason(ReportReason.불법_광고_홍보.name())
			.reportType(ReportType.CHAT_ROOM.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/reports")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		// then
		ChatRoom updatedChatRoom = chatRoomProvider.findById(chatRoom.getId());
		List<ChatRoomMember> members = updatedChatRoom.getChatRoomMembers();

		ChatRoomMember reporterMember = members.stream()
			.filter(m -> m.getMember().getId().equals(reporter.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(reporterMember.getIsChatRoomDeleted()).isTrue();
	}

	@Test
	@DisplayName("회원은 디렉터를 신고할 수 있다. (신고 대상이 존재하지 않을 때)")
	void save_whenReportedNotFound_throwsException() throws Exception {
		// given
		Member reporter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(reporter.getId());

		ReportRequest request = ReportRequest.builder()
			.reportedId(99999999L)
			.reason(ReportReason.불법_광고_홍보.name())
			.reportType(ReportType.CHAT_ROOM.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/reports")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(MemberException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(MemberException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(MemberException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("회원은 디렉터를 신고할 수 있다. (자기 자신을 신고할 때)")
	void save_whenSelfReport_throwsException() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleMember(director.getId());

		ReportRequest request = ReportRequest.builder()
			.reportedId(director.getId())
			.reason(ReportReason.불법_광고_홍보.name())
			.reportType(ReportType.CHAT_ROOM.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/reports")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(MemberBlockException.CANNOT_BLOCK_SELF.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(MemberBlockException.CANNOT_BLOCK_SELF.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(MemberBlockException.CANNOT_BLOCK_SELF.getCode()));
	}

	@Test
	@DisplayName("회원은 디렉터를 신고할 수 있다. (이미 차단된 사용자일 때)")
	void save_whenAlreadyBlocked_throwsException() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member reporter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(reporter.getId());

		// 이미 차단
		memberBlockProvider.save(reporter, director);

		ReportRequest request = ReportRequest.builder()
			.reportedId(director.getId())
			.reason(ReportReason.불법_광고_홍보.name())
			.reportType(ReportType.CHAT_ROOM.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/reports")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(MemberBlockException.ALREADY_BLOCKED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(MemberBlockException.ALREADY_BLOCKED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(MemberBlockException.ALREADY_BLOCKED.getCode()));
	}

	@Test
	@DisplayName("회원은 디렉터를 신고할 수 있다. (진행 중인 제안이 있을 때)")
	void save_whenOngoingEstimateExists_throwsException() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member reporter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(reporter.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest ongoingRequest = serviceRequestProvider.saveWithIsOngoingTrue(
			directorService, reporter, LocalDateTime.now());
		serviceEstimateProvider.saveOngoing(directorInfo, ongoingRequest, LocalDateTime.now());

		ReportRequest request = ReportRequest.builder()
			.reportedId(director.getId())
			.reason(ReportReason.불법_광고_홍보.name())
			.reportType(ReportType.CHAT_ROOM.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/reports")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(
					MemberBlockException.CANNOT_BLOCK_DURING_ONGOING_ESTIMATE.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(
				MemberBlockException.CANNOT_BLOCK_DURING_ONGOING_ESTIMATE.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(MemberBlockException.CANNOT_BLOCK_DURING_ONGOING_ESTIMATE.getCode()));
	}

	@Test
	@DisplayName("회원은 디렉터를 신고할 수 있다. (필수 필드가 누락되었을 때 - reportType 이 잘못되었을때)")
	void save_whenReportedIdIsNull_throwsException() throws Exception {
		// given
		Member reporter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(reporter.getId());

		ReportRequest request = ReportRequest.builder()
			.reportedId(null)
			.reason(ReportReason.불법_광고_홍보.name())
			.reportType("INAPPROPRIATE_BEHAVIOR")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/reports")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("회원은 디렉터를 신고할 수 있다. (필수 필드가 누락되었을 때 - reason 이 잘못되었을때)")
	void save_whenReasonIsBlank_throwsException() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member reporter = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(reporter.getId());

		ReportRequest request = ReportRequest.builder()
			.reportedId(director.getId())
			.reason(INVALID_STR)
			.reportType(ReportType.CHAT_ROOM.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/reports")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(MemberReportException.INVALID_REASON.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(MemberReportException.INVALID_REASON.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(MemberReportException.INVALID_REASON.getCode()));
	}

}
