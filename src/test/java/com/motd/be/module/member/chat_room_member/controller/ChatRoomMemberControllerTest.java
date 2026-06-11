package com.motd.be.module.member.chat_room_member.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.ChatRoomMemberException;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class ChatRoomMemberControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("멤버는 채팅방에서 나갈 수 있다 (정상)")
	void deleteChatRoomMember_success() throws Exception {
		// given
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now()));
		Member member = memberProvider.saveMember(SignInPlatform.GOOGLE);

		ChatRoom chatRoom = chatRoomProvider.save();
		ChatRoomMember directorChatMember = chatRoomMemberProvider.saveDirector(chatRoom, director);
		ChatRoomMember normalChatMember = chatRoomMemberProvider.saveMember(chatRoom, member);

		entityManager.flush();
		entityManager.clear();

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());
		// when
		mockMvc.perform(delete("/api/chat-rooms/{chatRoomId}", chatRoom.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()),
					new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		ChatRoomMember updatedNormalChatMember = chatRoomMemberProvider.findById(normalChatMember.getId());

		assertThat(updatedNormalChatMember.getIsChatRoomDeleted()).isTrue();

		// 디렉터는 채팅방 삭제상태가 아닌걸 검증한다.
		ChatRoomMember updatedDirectorMember = chatRoomMemberProvider.findById(directorChatMember.getId());
		assertThat(updatedDirectorMember.getIsChatRoomDeleted()).isFalse();

		// 채팅방 존재 여부 검증
		ChatRoom existingChatRoom = chatRoomProvider.findById(chatRoom.getId());
		assertThat(existingChatRoom.getIsDeleted()).isFalse();
	}

	@Test
	@DisplayName("멤버는 채팅방에서 나갈 수 있다. (이때 채팅방에 속하지 않은 회원일때)")
	void deleteChatRoomMember_notInChatRoom() throws Exception {
		// given
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now()));
		Member otherMember = memberProvider.saveMember(SignInPlatform.GOOGLE);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);

		Jwt jwt = generateTokenWithMemberIdRoleMember(otherMember.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(delete("/api/chat-rooms/{chatRoomId}", chatRoom.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()),
					new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isNotFound())
			.andExpect(
				jsonPath("$.status").value(ChatRoomMemberException.NOT_FOUND_IN_CHAT_ROOM.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(ChatRoomMemberException.NOT_FOUND_IN_CHAT_ROOM.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(ChatRoomMemberException.NOT_FOUND_IN_CHAT_ROOM.getCode()));
	}

	@Test
	@DisplayName("멤버는 채팅방에서 나갈 수 있다. (이때 존재하지 않는 채팅방일때)")
	void deleteChatRoomMember_chatRoomNotFound() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.GOOGLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		Long nonExistingChatRoomId = 99999L;

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(delete("/api/chat-rooms/{chatRoomId}", nonExistingChatRoomId)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()),
					new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isNotFound())
			.andExpect(
				jsonPath("$.status").value(ChatRoomMemberException.NOT_FOUND_IN_CHAT_ROOM.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(ChatRoomMemberException.NOT_FOUND_IN_CHAT_ROOM.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(ChatRoomMemberException.NOT_FOUND_IN_CHAT_ROOM.getCode()));
	}

}
