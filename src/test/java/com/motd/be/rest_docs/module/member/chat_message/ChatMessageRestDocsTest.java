package com.motd.be.rest_docs.module.member.chat_message;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.rest_docs.Utils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.chat_message.dto.request.ChatMessageSendFileRequest;
import com.motd.be.module.member.chat_message.dto.response.ChatMessageFindAllResponse;
import com.motd.be.module.member.chat_message.dto.response.ChatMessageResponse;
import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class ChatMessageRestDocsTest extends BaseRestDocsTest {

	@Test
	void 채팅_메시지_목록_조회() throws Exception {
		authenticationSetUp();

		MemberResponse sender1 = MemberResponse.builder()
			.id(5L)
			.nickname("홍길동")
			.profileImageUrl("https://example.com/profile.jpg")
			.build();

		MemberResponse sender2 = MemberResponse.builder()
			.id(6L)
			.nickname("김철수")
			.profileImageUrl("https://example.com/profile2.jpg")
			.build();

		ChatMessageResponse message1 = ChatMessageResponse.builder()
			.id(100L)
			.sender(sender1)
			.content("안녕하세요")
			.chatMessageType("TEXT")
			.isReadByOpponent(true)
			.sendAt("2024-01-28 10:30:00")
			.files(null)
			.estimate(null)
			.review(null)
			.build();

		ChatMessageResponse message2 = ChatMessageResponse.builder()
			.id(99L)
			.sender(sender2)
			.content("반갑습니다")
			.chatMessageType("TEXT")
			.isReadByOpponent(true)
			.sendAt("2024-01-28 10:25:00")
			.files(null)
			.estimate(null)
			.review(null)
			.build();

		ChatMessageFindAllResponse response = ChatMessageFindAllResponse.builder()
			.page(0)
			.hasNext(false)
			.chatMessages(List.of(message1, message2))
			.build();

		given(chatMessageFacade.findAllByChatRoomId(anyLong(), anyLong(), any())).willReturn(response);

		mockMvc.perform(get("/api/chat-messages")
				.param("chatRoomId", "1")
				.param("lastMessageId", "20")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andDo(document("chat-message-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				org.springframework.restdocs.request.RequestDocumentation.queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("chatRoomId")
						.description("채팅방 ID"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("lastMessageId")
						.optional()
						.description("마지막으로 조회한 메시지 ID (기본값: null)")
				),

				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER).description("페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
					fieldWithPath("chatMessages").type(JsonFieldType.ARRAY).description("채팅 메시지 목록"),
					fieldWithPath("chatMessages[].id").type(JsonFieldType.NUMBER).description("메시지 ID"),
					fieldWithPath("chatMessages[].sender").type(JsonFieldType.OBJECT).description("발신자 정보"),
					fieldWithPath("chatMessages[].sender.id").type(JsonFieldType.NUMBER).description("발신자 ID"),
					fieldWithPath("chatMessages[].sender.nickname").type(JsonFieldType.STRING)
						.description("발신자 닉네임"),
					fieldWithPath("chatMessages[].sender.profileImageUrl").type(JsonFieldType.STRING)
						.description("발신자 프로필 이미지 URL").optional(),
					fieldWithPath("chatMessages[].sender.isWithdrawal").type(JsonFieldType.OBJECT)
						.description("탈퇴 여부").optional(),
					fieldWithPath("chatMessages[].content").type(JsonFieldType.STRING).description("메시지 내용")
						.optional(),
					fieldWithPath("chatMessages[].chatMessageType").type(JsonFieldType.STRING)
						.description("메시지 타입 (TEXT, IMAGE, FILE, ESTIMATE, REVIEW)"),
					fieldWithPath("chatMessages[].isReadByOpponent").type(JsonFieldType.BOOLEAN)
						.description("상대방이 읽었는지 여부"),
					fieldWithPath("chatMessages[].sendAt").type(JsonFieldType.STRING).description("전송 시간"),
					fieldWithPath("chatMessages[].files").type(JsonFieldType.OBJECT).description("첨부 파일 목록")
						.optional(),
					fieldWithPath("chatMessages[].estimate").type(JsonFieldType.OBJECT).description("제안서 정보")
						.optional(),
					fieldWithPath("chatMessages[].review").type(JsonFieldType.OBJECT).description("리뷰 정보").optional()
				),

				resource(builder()
					.tag("💬 채팅 메시지 API")
					.summary("채팅 메시지 목록 조회")
					.description("특정 채팅방의 메시지 목록을 커서 기반 페이징으로 조회합니다. "
						+ "lastMessageId가 null이면 최신 메시지부터 조회하고, 있으면 해당 메시지보다 이전 메시지를 조회합니다.")
					.build())
			));
	}

	@Test
	void 채팅방_이미지_메시지_전송() throws Exception {
		authenticationSetUp();

		ChatMessageSendFileRequest request = ChatMessageSendFileRequest.builder()
			.fileIds(List.of(1L, 2L, 3L))
			.chatRoomId(1L)
			.fileType(UploadFileType.DOCUMENT.name())
			.build();

		willDoNothing().given(chatMessageFacade)
			.sendFileMessage(anyLong(), any(ChatMessageSendFileRequest.class));

		mockMvc.perform(post("/api/chat-messages/files", 1L)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isCreated())
			.andDo(document("chat-message-send-files",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("fileIds").type(JsonFieldType.ARRAY).description("이미지 또는 파일 ID 목록").optional(),
					fieldWithPath("chatRoomId").type(JsonFieldType.NUMBER).description("채팅방 ID"),
					fieldWithPath("fileType").type(JsonFieldType.STRING)
						.attributes(enumFormat(UploadFileType.class, Enum::name))
						.description("전송할 파일 유형")

				),

				resource(builder()
					.tag("💬 채팅 메시지 API")
					.summary("채팅방 이미지 또는 파일 메시지 전송")
					.description("채팅방에 이미지 메시지를 전송합니다. 이미지는 미리 업로드된 이미지 ID 목록을 전달합니다.")
					.build())
			));
	}

	@Test
	void 채팅메시지_삭제() throws Exception {
		authenticationSetUp();

		willDoNothing().given(chatMessageFacade)
			.delete(anyLong(), anyLong());

		mockMvc.perform(delete("/api/chat-messages/{chatMessageId}", 1L)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isNoContent())
			.andDo(document("chat-message-delete",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("chatMessageId")
						.description("삭제할 채팅 메시지 ID")
				),

				resource(builder()
					.tag("💬 채팅 메시지 API")
					.summary("채팅 메시지 삭제")
					.description("지정한 채팅 메시지를 삭제합니다.")
					.build())
			));
	}
}
