package com.motd.be.rest_docs.module.admin.chat_room;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;
import static com.motd.be.rest_docs.Utils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.admin.chat_message.dto.response.ChatMessageFindAllResponseForAdmin;
import com.motd.be.module.admin.chat_message.dto.response.ChatMessageResponseForAdmin;
import com.motd.be.module.admin.chat_room.dto.response.ChatRoomFindDetailResponseForAdmin;
import com.motd.be.module.admin.member.dto.response.MemberSummaryForAdmin;
import com.motd.be.module.admin.member.dto.response.MemberWithProfileImageResponseForAdmin;

import jakarta.servlet.http.Cookie;

@RestDocsTest
class ChatRoomAdminRestDocsTest extends BaseRestDocsTest {

	@Test
	@DisplayName("관리자 채팅방 상세 조회 (serviceEstimateId로 조회)")
	void findDetail() throws Exception {
		// given
		authenticationSetUp();

		ChatRoomFindDetailResponseForAdmin response = ChatRoomFindDetailResponseForAdmin.builder()
			.chatRoomId(1L)
			.serviceEstimateId(100L)
			.director(MemberWithProfileImageResponseForAdmin.builder()
				.id(10L)
				.nickname("전문디렉터")
				.profileImageUrl(PROFILE_IMAGE_STR)
				.build())
			.member(MemberWithProfileImageResponseForAdmin.builder()
				.id(5L)
				.nickname("일반회원")
				.profileImageUrl(PROFILE_IMAGE_STR)
				.build())
			.isDirectorPaid(Boolean.TRUE)
			.createdAt(formatToDateString(LocalDateTime.now()))
			.build();

		given(chatRoomFacadeForAdmin.findDetailByServiceEstimateId(anyLong()))
			.willReturn(response);

		// when & then
		mockMvc.perform(get("/api/admin/chat-rooms")
				.param("serviceEstimateId", "100")
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)))
			.andExpect(status().isOk())
			.andDo(document("admin-chat-room-find-detail",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				org.springframework.restdocs.request.RequestDocumentation.queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("serviceEstimateId")
						.description("서비스 제안 ID")
				),

				responseFields(
					fieldWithPath("chatRoomId").type(JsonFieldType.NUMBER)
						.description("채팅방 ID"),
					fieldWithPath("serviceEstimateId").type(JsonFieldType.NUMBER)
						.description("서비스 제안 ID"),
					fieldWithPath("director").type(JsonFieldType.OBJECT)
						.description("디렉터 정보"),
					fieldWithPath("director.id").type(JsonFieldType.NUMBER)
						.description("디렉터 회원 ID"),
					fieldWithPath("director.nickname").type(JsonFieldType.STRING)
						.description("디렉터 닉네임"),
					fieldWithPath("director.profileImageUrl").type(JsonFieldType.STRING)
						.description("디렉터 프로필 이미지 URL"),
					fieldWithPath("member").type(JsonFieldType.OBJECT)
						.description("일반 회원 정보"),
					fieldWithPath("member.id").type(JsonFieldType.NUMBER)
						.description("회원 ID"),
					fieldWithPath("member.nickname").type(JsonFieldType.STRING)
						.description("회원 닉네임"),
					fieldWithPath("member.profileImageUrl").type(JsonFieldType.STRING)
						.description("회원 프로필 이미지 URL"),
					fieldWithPath("isDirectorPaid").type(JsonFieldType.BOOLEAN)
						.description("디렉터 결제 여부"),
					fieldWithPath("createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("채팅방 생성 일시")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 채팅방 상세 조회")
					.description("""
						## 설명
						관리자가 serviceEstimateId를 통해 채팅방 상세 정보를 조회합니다.
						
						## 사용 예시
						서비스 제안 상세 페이지에서 채팅방으로 이동할 때 사용합니다.
						""")
					.build())
			));
	}

	@Test
	@DisplayName("관리자 채팅 메시지 조회 (serviceEstimateId로 조회)")
	void findMessages() throws Exception {
		// given
		authenticationSetUp();

		ChatMessageResponseForAdmin message = ChatMessageResponseForAdmin.builder()
			.id(1L)
			.sender(MemberSummaryForAdmin.builder()
				.id(10L)
				.nickname("전문디렉터")
				.build())
			.chatMessageType("TEXT")
			.content("안녕하세요")
			.sendAt(formatToDateString(LocalDateTime.now()))
			.build();

		ChatMessageFindAllResponseForAdmin response = ChatMessageFindAllResponseForAdmin.builder()
			.page(0)
			.hasNext(false)
			.chatMessages(List.of(message))
			.build();

		given(chatRoomFacadeForAdmin.findMessagesByServiceEstimateId(anyLong(), any()))
			.willReturn(response);

		// when & then
		mockMvc.perform(get("/api/admin/chat-rooms/messages")
				.param("serviceEstimateId", "100")
				.param(LAST_MESSAGE_ID, ZERO_STR)
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)))
			.andExpect(status().isOk())
			.andDo(document("admin-chat-message-find-messages",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				org.springframework.restdocs.request.RequestDocumentation.queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("serviceEstimateId")
						.description("서비스 제안 ID"),

					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(LAST_MESSAGE_ID)
						.optional()
						.description("커서 기반 페이징을 위한 마지막 메시지 ID (선택적, 첫 조회 시 생략)")
				),

				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
					fieldWithPath("chatMessages").type(JsonFieldType.ARRAY).description("채팅 메시지 목록"),
					fieldWithPath("chatMessages[].id").type(JsonFieldType.NUMBER).description("메시지 ID"),
					fieldWithPath("chatMessages[].chatMessageType").type(JsonFieldType.STRING)
						.description("메시지 타입"),
					fieldWithPath("chatMessages[].content").type(JsonFieldType.STRING)
						.optional()
						.description("메시지 내용"),
					fieldWithPath("chatMessages[].sendAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("전송 시각"),

					fieldWithPath("chatMessages[].sender").type(JsonFieldType.OBJECT)
						.description("발신자 정보"),
					fieldWithPath("chatMessages[].sender.id").type(JsonFieldType.NUMBER)
						.description("발신자 ID"),
					fieldWithPath("chatMessages[].sender.nickname").type(JsonFieldType.STRING)
						.description("발신자 닉네임")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 채팅 메시지 조회")
					.description("""
						## 설명
						관리자가 serviceEstimateId를 통해 채팅 메시지를 커서 기반으로 조회합니다.
						
						## 사용 예시
						채팅방 상세에서 메시지를 추가로 불러올 때 사용합니다.
						""")
					.build())
			));
	}
}
