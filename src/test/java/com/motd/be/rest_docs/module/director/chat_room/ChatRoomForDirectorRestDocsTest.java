package com.motd.be.rest_docs.module.director.chat_room;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.director.chat_room.dto.response.ChatRoomFindAllResponseForDirector;
import com.motd.be.module.director.chat_room.dto.response.ChatRoomFindChatRoomServicesResponseForDirector;
import com.motd.be.module.director.chat_room.dto.response.ChatRoomResponseForDirector;
import com.motd.be.module.director.chat_room.dto.response.ChatRoomUnreadCountResponseForDirector;
import com.motd.be.module.director.director_service.dto.response.DirectorServiceWithFullNameResponseForDirector;
import com.motd.be.module.director.location.dto.response.LocationResponseForDirector;
import com.motd.be.module.director.member.dto.response.MemberResponseForDirector;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class ChatRoomForDirectorRestDocsTest extends BaseRestDocsTest {

	@Test
	void 채팅방_전체_조회() throws Exception {
		authenticationSetUp();

		ChatRoomResponseForDirector chatRoom = ChatRoomResponseForDirector.builder()
			.id(1L)
			.lastMessage("안녕하세요")
			.lastMessageSendAt(formatToDateString(LocalDateTime.now()))
			.opponent(MemberResponseForDirector.builder()
				.id(1L)
				.nickname(NICKNAME_STR)
				.profileImageUrl(PROFILE_IMAGE_STR)
				.isWithdrawal(false)
				.build())
			.unreadCount(2)
			.serviceResponse(DirectorServiceWithFullNameResponseForDirector.builder()
				.id(1L)
				.name("헬스 케어")
				.fullName("건강 > 헬스 케어")
				.build())
			.locationResponse(List.of(LocationResponseForDirector.builder()
				.id(1L)
				.name("송파구")
				.fullName("서울시 송파구")
				.build()))
			.price(50000L)
			.isHired(Boolean.FALSE)
			.isInTransaction(Boolean.FALSE)
			.build();

		ChatRoomFindAllResponseForDirector response = ChatRoomFindAllResponseForDirector.builder()
			.page(0)
			.hasNext(false)
			.chatRooms(List.of(chatRoom))
			.build();

		given(chatRoomFacadeForDirector.findAll(anyLong(), any(), anyBoolean(), any(), any(), anyInt()))
			.willReturn(response);

		mockMvc.perform(get("/api/directors/chat-rooms")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.param(DIRECTOR_SERVICE_ID_STR, "1")
				.param("showOnlyUnread", "false")
				.param("status", "ACTIVE")
				.param("word", "검색어")
				.param(PAGE_STR, ZERO_STR)
			)
			.andExpect(status().isOk())
			.andDo(document("chat-room-find-all-for-director",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(DIRECTOR_SERVICE_ID_STR)
						.optional()
						.description("서비스 ID (필터링용) 없으면 null"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("showOnlyUnread")
						.optional()
						.description("읽지 않은 메시지가 있는 채팅방만 보기 여부 (true/false)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("status")
						.optional()
						.description("채팅방 상태 (PENDING : 제안 진행중, ONGOING: 거래중, null : 전체)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("word")
						.optional()
						.description("검색어 (상대방 닉네임, 디렉터 서비스) 없으면 null"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(PAGE_STR)
						.optional()
						.description("페이지 번호 (0부터 시작)")
				),

				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
					fieldWithPath("chatRooms").type(JsonFieldType.ARRAY).description("채팅방 목록"),
					fieldWithPath("chatRooms[].id").type(JsonFieldType.NUMBER).description("채팅방 ID"),
					fieldWithPath("chatRooms[].lastMessage").type(JsonFieldType.STRING).description("마지막 메세지 내용"),
					fieldWithPath("chatRooms[].lastMessageSendAt").type(JsonFieldType.STRING)
						.description("마지막 메세지 전송 시각 문자열"),
					fieldWithPath("chatRooms[].unreadCount").type(JsonFieldType.NUMBER).description("안읽은 메세지 수"),

					fieldWithPath("chatRooms[].opponent").type(JsonFieldType.OBJECT).description("상대방 정보"),
					fieldWithPath("chatRooms[].opponent.id").type(JsonFieldType.NUMBER).
						description("상대방 회원 ID"),
					fieldWithPath("chatRooms[].opponent.nickname").type(JsonFieldType.STRING)
						.description("상대방 닉네임"),
					fieldWithPath("chatRooms[].opponent.profileImageUrl").type(JsonFieldType.STRING)
						.description("상대방 프로필 이미지 URL"),
					fieldWithPath("chatRooms[].opponent.isWithdrawal").type(JsonFieldType.BOOLEAN)
						.description("회원탈퇴 여부"),

					fieldWithPath("chatRooms[].serviceResponse").type(JsonFieldType.OBJECT).description("디렉터 서비스 정보"),
					fieldWithPath("chatRooms[].serviceResponse.id").type(JsonFieldType.NUMBER)
						.description("디렉터 서비스 ID"),
					fieldWithPath("chatRooms[].serviceResponse.name").type(JsonFieldType.STRING)
						.description("디렉터 서비스 이름"),
					fieldWithPath("chatRooms[].serviceResponse.fullName").type(JsonFieldType.STRING)
						.description("디렉터 서비스 전체 경로 이름"),
					fieldWithPath("chatRooms[].locationResponse").type(JsonFieldType.ARRAY)
						.description("위치 목록"),
					fieldWithPath("chatRooms[].locationResponse[].id").type(JsonFieldType.NUMBER)
						.description("위치 ID"),
					fieldWithPath("chatRooms[].locationResponse[].name").type(JsonFieldType.STRING)
						.description("위치 이름"),
					fieldWithPath("chatRooms[].locationResponse[].fullName").type(JsonFieldType.STRING)
						.description("위치 전체 경로 이름"),
					fieldWithPath("chatRooms[].price").type(JsonFieldType.NUMBER).description("제안 가격 (마지막 서비스 제안 기준)"),
					fieldWithPath("chatRooms[].isHired").type(JsonFieldType.BOOLEAN)
						.description("해당 서비스가 고용되었는지 여부"),
					fieldWithPath("chatRooms[].isInTransaction").type(JsonFieldType.BOOLEAN)
						.description("해당 채팅방이 거래중인지 여부")
				),

				resource(builder()
					.tag("💬 채팅방 API")
					.summary("채팅방 전체 조회 (디렉터)")
					.description("디렉터 가 참여한(또는 조회 가능한) 채팅방 목록을 조회합니다.")
					.build())
			));
	}

	@Test
	void 채팅방_서비스_목록_조회() throws Exception {
		authenticationSetUp();

		DirectorServiceWithFullNameResponseForDirector serviceResponse1 = DirectorServiceWithFullNameResponseForDirector.builder()
			.id(1L)
			.name("헬스 케어")
			.fullName("건강 > 헬스 케어")
			.build();

		DirectorServiceWithFullNameResponseForDirector serviceResponse2 = DirectorServiceWithFullNameResponseForDirector.builder()
			.id(2L)
			.name("요가")
			.fullName("건강 > 요가")
			.build();

		ChatRoomFindChatRoomServicesResponseForDirector response = ChatRoomFindChatRoomServicesResponseForDirector.builder()
			.services(List.of(serviceResponse1, serviceResponse2))
			.build();

		given(chatRoomFacadeForDirector.findChatRoomServices(anyLong()))
			.willReturn(response);

		mockMvc.perform(get("/api/directors/chat-rooms/services")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("chat-room-find-services-for-director",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("services").type(JsonFieldType.ARRAY).description("채팅방에서 사용된 서비스 목록"),
					fieldWithPath("services[].id").type(JsonFieldType.NUMBER).description("서비스 ID"),
					fieldWithPath("services[].name").type(JsonFieldType.STRING).description("서비스 이름"),
					fieldWithPath("services[].fullName").type(JsonFieldType.STRING).description("서비스 전체 경로 이름")
				),

				resource(builder()
					.tag("💬 채팅방 API")
					.summary("채팅방 서비스 목록 조회 (디렉터)")
					.description("디렉터가 참여한 채팅방에서 사용된 디렉터 서비스 목록을 조회합니다.")
					.build())
			));
	}

	@Test
	void 채팅방_읽지_않은_메시지_총_개수_조회() throws Exception {
		authenticationSetUp();

		ChatRoomUnreadCountResponseForDirector response = ChatRoomUnreadCountResponseForDirector.builder()
			.totalUnreadCount(15)
			.build();

		given(chatRoomFacadeForDirector.countTotalUnreadMessages(anyLong()))
			.willReturn(response);

		mockMvc.perform(get("/api/directors/chat-rooms/unread-count")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("chat-room-find-total-unread-count-for-director",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("totalUnreadCount").type(JsonFieldType.NUMBER)
						.description("전체 채팅방의 읽지 않은 메시지 총 개수")
				),

				resource(builder()
					.tag("💬 채팅방 API")
					.summary("읽지 않은 메시지 총 개수 조회 (디렉터용)")
					.description("멤버(또는 디렉터)가 참여한 모든 채팅방의 읽지 않은 메시지 총 개수를 조회합니다.")
					.build())
			));
	}
}
