package com.motd.be.module.member.member_block.controller;

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
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.member_block.dto.request.MemberBlockRequest;
import com.motd.be.module.member.member_block.dto.response.MemberBlockCheckResponse;
import com.motd.be.module.member.member_block.dto.response.MemberBlockFindAllResponse;
import com.motd.be.module.member.member_block.entity.MemberBlock;
import com.motd.be.module.member.member_director_favorite.entity.MemberDirectorFavorite;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class MemberBlockControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("회원이 다른 회원을 차단할 수 있다.")
	void save_success() throws Exception {
		// given
		Member blocker = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member blocked = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(blocker.getId());

		MemberBlockRequest request = MemberBlockRequest.builder()
			.blockedId(blocked.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/block")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		// then
		List<MemberBlock> blocks = memberBlockProvider.findAll();
		assertThat(blocks).hasSize(1);
		MemberBlock block = blocks.get(0);
		assertThat(block.getBlocker().getId()).isEqualTo(blocker.getId());
		assertThat(block.getBlocked().getId()).isEqualTo(blocked.getId());
	}

	@Test
	@DisplayName("회원이 다른 회원을 차단할 수 있다. (디렉터가 특정 회원을 차단하는 경우)")
	void save_successAsDirector() throws Exception {
		// given
		Member blocker = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now()));
		Member blocked = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(blocker.getId());

		MemberBlockRequest request = MemberBlockRequest.builder()
			.blockedId(blocked.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/block")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		// then
		List<MemberBlock> blocks = memberBlockProvider.findAll();
		assertThat(blocks).hasSize(1);
		MemberBlock block = blocks.get(0);
		assertThat(block.getBlocker().getId()).isEqualTo(blocker.getId());
		assertThat(block.getBlocked().getId()).isEqualTo(blocked.getId());
	}

	@Test
	@DisplayName("회원이 다른 회원을 차단할 수 있다. (다른 디렉터와 진행중인 제안이 존재하는 경우.)")
	void saveWhenOngoingEstimateWithOtherDirectorExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, otherDirectorInfo);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorService parentService = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorServiceProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, member,
			LocalDateTime.now());
		serviceEstimateProvider.saveOngoing(otherDirectorInfo, serviceRequest, LocalDateTime.now());

		MemberBlockRequest request = MemberBlockRequest.builder()
			.blockedId(director.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/block")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		// then
		List<MemberBlock> blocks = memberBlockProvider.findAll();
		assertThat(blocks).hasSize(1);
		MemberBlock block = blocks.get(0);
		assertThat(block.getBlocker().getId()).isEqualTo(member.getId());
		assertThat(block.getBlocked().getId()).isEqualTo(director.getId());
	}

	@Test
	@DisplayName("회원이 다른 회원을 차단할 수 있다. (즐겨찾기가 되어있을때)")
	void saveWhenMemberDirectorFavoriteExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// 즐겨찾기 저장
		MemberDirectorFavorite memberDirectorFavorite = memberDirectorFavoriteProvider.save(member, director);

		MemberBlockRequest request = MemberBlockRequest.builder()
			.blockedId(director.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/block")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		// then
		List<MemberBlock> blocks = memberBlockProvider.findAll();
		assertThat(blocks).hasSize(1);
		MemberBlock block = blocks.get(0);
		assertThat(block.getBlocker().getId()).isEqualTo(member.getId());
		assertThat(block.getBlocked().getId()).isEqualTo(director.getId());

		// 즐겨찾기 해제 여부 확인
		assertThat(memberDirectorFavoriteProvider.findById(memberDirectorFavorite.getId())).isNull();
	}

	@Test
	@DisplayName("회원이 다른 회원을 차단할 수 있다. (둘사이 채팅방이 존재할때)")
	void saveWhenChatRoomExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(requester.getId());

		DirectorService parentService = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorServiceProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, requester);
		ServiceEstimate estimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		chatRoomMemberProvider.saveMember(chatRoom, requester);
		chatRoomMemberProvider.saveDirector(chatRoom, director);

		MemberBlockRequest request = MemberBlockRequest.builder()
			.blockedId(director.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/block")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		// then
		List<MemberBlock> blocks = memberBlockProvider.findAll();
		assertThat(blocks).hasSize(1);
		MemberBlock block = blocks.get(0);
		assertThat(block.getBlocker().getId()).isEqualTo(requester.getId());
		assertThat(block.getBlocked().getId()).isEqualTo(director.getId());

		// 채팅방 나감처리여부 확인
		ChatRoom updatedChatRoom = chatRoomProvider.findById(chatRoom.getId());

		List<ChatRoomMember> chatRoomMembers = updatedChatRoom.getChatRoomMembers();
		assertThat(chatRoomMembers).hasSize(2);
		ChatRoomMember requesterMember = chatRoomMembers.stream()
			.filter(member -> member.getMember().getId().equals(requester.getId()))
			.findFirst()
			.orElseThrow();
		assertThat(requesterMember.getIsChatRoomDeleted()).isTrue();
	}

	@Test
	@DisplayName("회원이 다른 회원을 차단할 수 있다. (둘사이 채팅방이 2개 존재할떄)")
	void saveWhenMoreThanOneChatRoomExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, otherDirectorInfo);

		DirectorService parentService = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorServiceProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, directorService);
		directorServiceMappingProvider.save(otherDirectorInfo, directorService);

		// director -> director / otherDirector -> requester
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, otherDirector);
		ServiceEstimate estimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		chatRoomMemberProvider.saveMember(chatRoom, otherDirector);
		chatRoomMemberProvider.saveDirector(chatRoom, director);

		// director -> requester / otherDirector -> director
		ServiceRequest otherServiceRequest = serviceRequestProvider.savePending(directorService, director);
		ServiceEstimate otherEstimate = serviceEstimateProvider.save(otherDirectorInfo, otherServiceRequest);

		ChatRoom otherChatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, otherEstimate);

		chatRoomMemberProvider.saveMember(otherChatRoom, director);
		chatRoomMemberProvider.saveDirector(otherChatRoom, otherDirector);

		MemberBlockRequest request = MemberBlockRequest.builder()
			.blockedId(director.getId())
			.build();

		Jwt jwt = generateTokenWithMemberIdRoleDirector(otherDirector.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/block")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		// then
		List<MemberBlock> blocks = memberBlockProvider.findAll();
		assertThat(blocks).hasSize(1);
		MemberBlock block = blocks.get(0);
		assertThat(block.getBlocker().getId()).isEqualTo(otherDirector.getId());
		assertThat(block.getBlocked().getId()).isEqualTo(director.getId());

		// 채팅방 나감처리여부 확인
		ChatRoom updatedChatRoom = chatRoomProvider.findById(chatRoom.getId());

		List<ChatRoomMember> chatRoomMembers = updatedChatRoom.getChatRoomMembers();
		assertThat(chatRoomMembers).hasSize(2);
		ChatRoomMember requesterMember = chatRoomMembers.stream()
			.filter(member -> member.getMember().getId().equals(otherDirector.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(requesterMember.getIsChatRoomDeleted()).isTrue();
		assertThat(requesterMember.getIsDirector()).isFalse();

		// 채팅방 나감처리 여부 확인
		ChatRoom updatedOtherChatRoom = chatRoomProvider.findById(otherChatRoom.getId());

		List<ChatRoomMember> otherChatRoomMembers = updatedOtherChatRoom.getChatRoomMembers();
		assertThat(otherChatRoomMembers).hasSize(2);
		ChatRoomMember otherRequesterMember = otherChatRoomMembers.stream()
			.filter(member -> member.getMember().getId().equals(otherDirector.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(otherRequesterMember.getIsChatRoomDeleted()).isTrue();
		assertThat(otherRequesterMember.getIsDirector()).isTrue();

	}

	@Test
	@DisplayName("회원이 다른 회원을 차단할 수 있다. (진행중인 제안이 존재하는 경우.)")
	void save_failDueToOngoingEstimate() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parentService = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorServiceProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, member,
			LocalDateTime.now());
		serviceEstimateProvider.saveOngoing(directorInfo, serviceRequest, LocalDateTime.now());

		MemberBlockRequest request = MemberBlockRequest.builder()
			.blockedId(member.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/block")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS)
				.value(MemberBlockException.CANNOT_BLOCK_DURING_ONGOING_ESTIMATE.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE)
				.value(MemberBlockException.CANNOT_BLOCK_DURING_ONGOING_ESTIMATE.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE)
				.value(MemberBlockException.CANNOT_BLOCK_DURING_ONGOING_ESTIMATE.getCode()));
	}

	@Test
	@DisplayName("회원이 다른 회원을 차단할 수 있다. (존재하지 않는 회원을 차단하는 경우)")
	void save_notFoundMember() throws Exception {
		// given
		Member blocker = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(blocker.getId());

		MemberBlockRequest request = MemberBlockRequest.builder()
			.blockedId(99999999L)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/block")
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
	@DisplayName("회원이 다른 회원을 차단할 수 있다. (이미 차단한 회원을 다시 차단하는 경우)")
	void save_alreadyBlocked() throws Exception {
		// given
		Member blocker = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member blocked = memberProvider.saveMember(SignInPlatform.APPLE);
		memberBlockProvider.save(blocker, blocked);

		Jwt jwt = generateTokenWithMemberIdRoleMember(blocker.getId());

		MemberBlockRequest request = MemberBlockRequest.builder()
			.blockedId(blocked.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/block")
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
	@DisplayName("회원이 다른 회원을 차단할 수 있다. (자기 자신을 차단하는 경우)")
	void save_cannotBlockSelf() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		MemberBlockRequest request = MemberBlockRequest.builder()
			.blockedId(member.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/block")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(MemberBlockException.CANNOT_BLOCK_SELF.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(MemberBlockException.CANNOT_BLOCK_SELF.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(MemberBlockException.CANNOT_BLOCK_SELF.getCode()));
	}

	@Test
	@DisplayName("차단한 회원을 차단 해제할 수 있다.")
	void delete_success() throws Exception {
		// given
		Member blocker = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member blocked = memberProvider.saveMember(SignInPlatform.APPLE);
		memberBlockProvider.save(blocker, blocked);

		Jwt jwt = generateTokenWithMemberIdRoleMember(blocker.getId());

		MemberBlockRequest request = MemberBlockRequest.builder()
			.blockedId(blocked.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/members/block")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		// then
		List<MemberBlock> blocks = memberBlockProvider.findAll();
		assertThat(blocks).isEmpty();
	}

	@Test
	@DisplayName("차단한 회원을 차단 해제할 수 있다. (차단하지 않은 회원을 차단 해제하는 경우)")
	void delete_notBlocked() throws Exception {
		// given
		Member blocker = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member blocked = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(blocker.getId());

		MemberBlockRequest request = MemberBlockRequest.builder()
			.blockedId(blocked.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/members/block")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(MemberBlockException.ALREADY_DELETED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(MemberBlockException.ALREADY_DELETED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(MemberBlockException.ALREADY_DELETED.getCode()));
	}

	@Test
	@DisplayName("차단한 회원을 차단 해제할 수 있다.(존재하지 않는 회원을 차단 해제하는 경우)")
	void delete_notFoundMember() throws Exception {
		// given
		Member blocker = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(blocker.getId());

		MemberBlockRequest request = MemberBlockRequest.builder()
			.blockedId(99999999L)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/members/block")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(MemberBlockException.ALREADY_DELETED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(MemberBlockException.ALREADY_DELETED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(MemberBlockException.ALREADY_DELETED.getCode()));
	}

	@Test
	@DisplayName("차단한 회원을 차단 해제할 수 있다.(자기 자신을 차단 해제하는 경우)")
	void delete_self() throws Exception {
		// given
		Member blocker = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member blocked = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(blocker.getId());

		MemberBlockRequest request = MemberBlockRequest.builder()
			.blockedId(blocker.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/members/block")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(MemberBlockException.CANNOT_UNBLOCK_SELF.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(MemberBlockException.CANNOT_UNBLOCK_SELF.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(MemberBlockException.CANNOT_UNBLOCK_SELF.getCode()));
	}

	@Test
	@DisplayName("차단 목록을 조회 할 수 있다.")
	void findAll_success() throws Exception {
		// given
		Member blocker = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member blocked1 = memberProvider.saveMember(SignInPlatform.APPLE);
		Member blocked2 = memberProvider.saveMember(SignInPlatform.GOOGLE);

		memberBlockProvider.save(blocker, blocked1);
		memberBlockProvider.save(blocker, blocked2);

		Jwt jwt = generateTokenWithMemberIdRoleMember(blocker.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/block")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		MemberBlockFindAllResponse response = objectMapper.readValue(responseJson, MemberBlockFindAllResponse.class);

		// then
		assertThat(response.getBlocks()).hasSize(2);
		assertThat(response.getBlocks())
			.extracting("id")
			.containsExactlyInAnyOrder(blocked1.getId(), blocked2.getId());
	}

	@Test
	@DisplayName("차단 목록을 조회 할 수 있다. (차단목록이 비어있는 경우)")
	void findAll_emptyList() throws Exception {
		// given
		Member blocker = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(blocker.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/block")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		MemberBlockFindAllResponse response = objectMapper.readValue(responseJson, MemberBlockFindAllResponse.class);

		// then
		assertThat(response.getBlocks()).isEmpty();
	}

	@Test
	@DisplayName("차단 목록을 조회 할 수 있다. (페이징 처리)")
	void findAll_withPaging() throws Exception {
		// given
		Member blocker = memberProvider.saveMember(SignInPlatform.KAKAO);

		for (int i = 0; i < 25; i++) {
			Member blocked = memberProvider.saveMember(SignInPlatform.APPLE);
			memberBlockProvider.save(blocker, blocked);
		}

		Jwt jwt = generateTokenWithMemberIdRoleMember(blocker.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String json = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/block")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(PAGE_STR, "1")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		MemberBlockFindAllResponse firstPage = objectMapper.readValue(json, MemberBlockFindAllResponse.class);

		// then
		assertThat(firstPage.getBlocks()).hasSize(5);
		assertThat(firstPage.getHasNext()).isFalse();
	}

	@Test
	@DisplayName("차단 목록을 조회 할 수 있다.(디렉터가 조회하는 경우)")
	void findAll_asDirector() throws Exception {
		// given
		Member blocker = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now()));
		Member blocked = memberProvider.saveMember(SignInPlatform.APPLE);
		memberBlockProvider.save(blocker, blocked);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(blocker.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/block")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		MemberBlockFindAllResponse response = objectMapper.readValue(responseJson, MemberBlockFindAllResponse.class);

		// then
		assertThat(response.getBlocks()).hasSize(1);
	}

	@Test
	@DisplayName("차단 여부를 조회할 수 있다.")
	void check_blockStatus() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member target = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		memberBlockProvider.save(member, target);

		entityManager.flush();
		entityManager.clear();

		// when & then - blocked case
		String blockedResponse = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/block/check")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(TARGET_MEMBER_ID, target.getId().toString()))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		MemberBlockCheckResponse blockedCheck = objectMapper.readValue(blockedResponse,
			MemberBlockCheckResponse.class);
		assertThat(blockedCheck.isBlocked()).isTrue();

		// when & then - not blocked
		Member other = memberProvider.saveMember(SignInPlatform.GOOGLE);
		entityManager.flush();
		entityManager.clear();

		String notBlockedResponse = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/block/check")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(TARGET_MEMBER_ID, other.getId().toString()))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		MemberBlockCheckResponse notBlockedCheck = objectMapper.readValue(notBlockedResponse,
			MemberBlockCheckResponse.class);
		assertThat(notBlockedCheck.isBlocked()).isFalse();
	}

	@Test
	@DisplayName("차단 여부를 조회할 수 있다.")
	void check_success() throws Exception {
		// given
		Member blocker = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member blocked = memberProvider.saveMember(SignInPlatform.APPLE);
		memberBlockProvider.save(blocker, blocked);

		Jwt jwt = generateTokenWithMemberIdRoleMember(blocker.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/block/check")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(TARGET_MEMBER_ID, blocked.getId().toString())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		MemberBlockCheckResponse response = objectMapper.readValue(responseJson, MemberBlockCheckResponse.class);

		// then
		assertThat(response.isBlocked()).isTrue();
	}

	@Test
	@DisplayName("차단 여부를 조회할 수 있다. (차단하지 않은 회원인 경우)")
	void check_notBlocked() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member target = memberProvider.saveMember(SignInPlatform.APPLE);

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/block/check")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(TARGET_MEMBER_ID, target.getId().toString())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		MemberBlockCheckResponse response = objectMapper.readValue(responseJson, MemberBlockCheckResponse.class);

		// then
		assertThat(response.isBlocked()).isFalse();
	}

	@Test
	@DisplayName("차단 여부를 조회할 수 있다. (디렉터가 조회하는 경우)")
	void check_asDirector() throws Exception {
		// given
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now()));
		Member blocked = memberProvider.saveMember(SignInPlatform.APPLE);
		memberBlockProvider.save(director, blocked);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/block/check")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(TARGET_MEMBER_ID, blocked.getId().toString())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		MemberBlockCheckResponse response = objectMapper.readValue(responseJson, MemberBlockCheckResponse.class);

		// then
		assertThat(response.isBlocked()).isTrue();
	}

	@Test
	@DisplayName("차단 여부를 조회할 수 있다. (차단을 당한경우)")
	void check_successWhenBlocked() throws Exception {
		// given
		Member blocker = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member blocked = memberProvider.saveMember(SignInPlatform.APPLE);
		memberBlockProvider.save(blocked, blocker);

		Jwt jwt = generateTokenWithMemberIdRoleMember(blocker.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/block/check")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(TARGET_MEMBER_ID, blocked.getId().toString())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		MemberBlockCheckResponse response = objectMapper.readValue(responseJson, MemberBlockCheckResponse.class);

		// then
		assertThat(response.isBlocked()).isFalse();
	}
}
