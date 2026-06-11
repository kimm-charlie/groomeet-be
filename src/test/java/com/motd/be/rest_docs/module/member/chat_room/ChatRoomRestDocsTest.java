package com.motd.be.rest_docs.module.member.chat_room;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.Constants.ID;
import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;
import static com.motd.be.rest_docs.Utils.*;
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
import com.motd.be.module.member.chat_message.dto.response.ChatMessageFindAllResponse;
import com.motd.be.module.member.chat_message.dto.response.ChatMessageResponse;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomFindAllResponse;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomFindChatRoomServicesResponse;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomFindDetailResponse;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomResponse;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomUnreadCountResponse;
import com.motd.be.module.member.director_service.dto.response.DirectorServiceWithFullNameResponse;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.location.dto.response.LocationResponse;
import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.module.member.review.dto.response.ReviewForChatResponse;
import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateForChatResponse;
import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateWithStatusResponse;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_request.dto.response.ServiceRequestForChatRoomFindDetailResponse;
import com.motd.be.module.member.service_request_wish_time.dto.response.WishTimeResponse;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class ChatRoomRestDocsTest extends BaseRestDocsTest {

	@Test
	void 채팅방_전체_조회() throws Exception {
		authenticationSetUp();

		ChatRoomResponse chatRoom = ChatRoomResponse.builder()
			.id(1L)
			.lastMessage("안녕하세요")
			.lastMessageSendAt(formatToDateString(LocalDateTime.now()))
			.opponent(MemberResponse.builder()
				.id(1L)
				.nickname(NICKNAME_STR)
				.profileImageUrl(PROFILE_IMAGE_STR)
				.isWithdrawal(false)
				.build())
			.unreadCount(2)
			.serviceResponse(DirectorServiceWithFullNameResponse.builder()
				.id(1L)
				.name("헬스 케어")
				.fullName("건강 > 헬스 케어")
				.build())
			.locationResponse(List.of(LocationResponse.builder()
				.id(1L)
				.name("송파구")
				.fullName("서울시 송파구")
				.build()))
			.price(50000L)
			.isHired(Boolean.FALSE)
			.isInTransaction(Boolean.FALSE)
			.build();

		ChatRoomFindAllResponse response = ChatRoomFindAllResponse.builder()
			.page(0)
			.hasNext(false)
			.chatRooms(List.of(chatRoom))
			.build();

		given(chatRoomFacade.findAllForPublic(anyLong(), any(), anyBoolean(), any(), any(), anyInt()))
			.willReturn(response);

		mockMvc.perform(get("/api/chat-rooms")
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
			.andDo(document("chat-room-find-all-for-public",
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
					fieldWithPath("chatRooms[].price").type(JsonFieldType.NUMBER)
						.description("제안 가격 (마지막 서비스 제안 기준)"),
					fieldWithPath("chatRooms[].isHired").type(JsonFieldType.BOOLEAN)
						.description("해당 서비스가 고용되었는지 여부"),
					fieldWithPath("chatRooms[].isInTransaction").type(JsonFieldType.BOOLEAN)
						.description("해당 채팅방이 거래중인지 여부")
				),

				resource(builder()
					.tag("💬 채팅방 API")
					.summary("채팅방 전체 조회 (멤버)")
					.description("멤버(또는 디렉터)가 참여한(또는 조회 가능한) 채팅방 목록을 조회합니다.")
					.build())
			));
	}

	@Test
	void 채팅방_서비스_목록_조회() throws Exception {
		authenticationSetUp();

		DirectorServiceWithFullNameResponse serviceResponse1 = DirectorServiceWithFullNameResponse.builder()
			.id(1L)
			.name("헬스 케어")
			.fullName("건강 > 헬스 케어")
			.build();

		DirectorServiceWithFullNameResponse serviceResponse2 = DirectorServiceWithFullNameResponse.builder()
			.id(2L)
			.name("요가")
			.fullName("건강 > 요가")
			.build();

		ChatRoomFindChatRoomServicesResponse response = ChatRoomFindChatRoomServicesResponse.builder()
			.services(List.of(serviceResponse1, serviceResponse2))
			.build();

		given(chatRoomFacade.findChatRoomServicesForPublic(anyLong()))
			.willReturn(response);

		mockMvc.perform(get("/api/chat-rooms/services")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("chat-room-find-services-for-public",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("services").type(JsonFieldType.ARRAY).description("채팅방에서 사용된 디렉터 서비스 목록"),
					fieldWithPath("services[].id").type(JsonFieldType.NUMBER).description("디렉터 서비스 ID"),
					fieldWithPath("services[].name").type(JsonFieldType.STRING).description("디렉터 서비스 이름"),
					fieldWithPath("services[].fullName").type(JsonFieldType.STRING).description("디렉터 서비스 전체 경로 이름")
				),

				resource(builder()
					.tag("💬 채팅방 API")
					.summary("채팅방 서비스 목록 조회 (멤버)")
					.description("멤버(또는 디렉터)가 참여한 채팅방에서 사용된 디렉터 서비스 목록을 조회합니다.")
					.build())
			));
	}

	@Test
	void 채팅방_상세_조회() throws Exception {
		authenticationSetUp();

		// 상대방 정보
		MemberResponse opponent = MemberResponse.builder()
			.id(2L)
			.nickname("상대방닉네임")
			.profileImageUrl("https://cdn.example.com/profile.jpg")
			.isWithdrawal(false)
			.build();

		// 서비스 정보
		DirectorServiceWithFullNameResponse serviceResponse = DirectorServiceWithFullNameResponse.builder()
			.id(1L)
			.name("헬스 케어")
			.fullName("건강 > 헬스 케어")
			.build();

		// 위치 정보
		LocationResponse locationResponse = LocationResponse.builder()
			.id(1L)
			.name("송파구")
			.fullName("서울시 송파구")
			.build();

		// 서비스 요청 정보
		ServiceRequestForChatRoomFindDetailResponse requestResponse = ServiceRequestForChatRoomFindDetailResponse.builder()
			.id(1L)
			.service(serviceResponse)
			.wishTimes(List.of(
				WishTimeResponse.builder().wishTime("2026.02.15 10:00").build(),
				WishTimeResponse.builder().wishTime("2026.02.15 14:00").build()
			))
			.location(List.of(locationResponse))
			.build();

		// 채팅 메시지 목록
		// TEXT 타입 메시지
		ChatMessageResponse textMessage = ChatMessageResponse.builder()
			.id(1L)
			.sender(opponent)
			.chatMessageType("TEXT")
			.content("안녕하세요, 문의드립니다.")
			.isReadByOpponent(Boolean.TRUE)
			.sendAt(formatToDateString(LocalDateTime.now().minusHours(3)))
			.files(null)
			.estimate(null)
			.build();

		// IMAGE 타입 메시지
		ChatMessageResponse imageMessage = ChatMessageResponse.builder()
			.id(2L)
			.sender(opponent)
			.chatMessageType("IMAGE")
			.content(null)
			.isReadByOpponent(Boolean.TRUE)
			.sendAt(formatToDateString(LocalDateTime.now().minusHours(2)))
			.files(List.of(
				FileResponse.builder()
					.id(1L)
					.fileUrl("https://cdn.example.com/chat/image1.jpg")
					.fileName(null)
					.fileSize("20MB")
					.fileType(UploadFileType.IMAGE)
					.build(),
				FileResponse.builder()
					.id(2L)
					.fileUrl("https://cdn.example.com/chat/image2.jpg")
					.fileName("파일명")
					.fileSize("20MB")
					.fileType(UploadFileType.DOCUMENT)
					.build()
			))
			.estimate(null)
			.build();

		// ESTIMATE 타입 메시지
		ChatMessageResponse estimateMessage = ChatMessageResponse.builder()
			.id(3L)
			.sender(MemberResponse.builder()
				.id(1L)
				.nickname(NICKNAME_STR)
				.profileImageUrl("https://cdn.example.com/my-profile.jpg")
				.isWithdrawal(false)
				.build())
			.chatMessageType("ESTIMATE")
			.content(null)
			.isReadByOpponent(Boolean.TRUE)
			.sendAt(formatToDateString(LocalDateTime.now().minusHours(1)))
			.files(null)
			.estimate(ServiceEstimateForChatResponse.builder()
				.id(1L)
				.title("헬스 케어 제안서")
				.content("1:1 PT 10회 기준 제안입니다.")
				.price(500000L)
				.service(DirectorServiceWithFullNameResponse.builder()
					.id(1L)
					.name("헬스 케어")
					.fullName("건강 > 헬스 케어")
					.build())
				.files(List.of(
					FileResponse.builder()
						.id(ID)
						.fileUrl(IMAGE_URL_STR)
						.fileUrl("https://cdn.example.com/chat/image1.jpg")
						.fileName(null)
						.fileSize("20MB")
						.fileType(UploadFileType.IMAGE)
						.build(),
					FileResponse.builder()
						.id(ID)
						.fileUrl(IMAGE_URL_STR)
						.fileName(null)
						.fileSize("20MB")
						.fileType(UploadFileType.IMAGE)
						.build()))
				.storeAddress(STORE_ADDRESS_STR)
				.scheduledAt(formatToDateString(LocalDateTime.now().plusDays(7)))
				.build())
			.build();

		// REVIEW 타입 메시지
		ChatMessageResponse reviewMessage = ChatMessageResponse.builder()
			.id(4L)
			.sender(MemberResponse.builder()
				.id(1L)
				.nickname(NICKNAME_STR)
				.profileImageUrl("https://cdn.example.com/my-profile.jpg")
				.isWithdrawal(false)
				.build())
			.chatMessageType("REVIEW_COMPLETED")
			.content(null)
			.isReadByOpponent(Boolean.TRUE)
			.sendAt(formatToDateString(LocalDateTime.now()))
			.files(null)
			.estimate(null)
			.review(ReviewForChatResponse.builder()
				.id(ID)
				.title("헬스 케어 서비스 후기")
				.content("정말 만족스러운 서비스였습니다!")
				.service(DirectorServiceWithFullNameResponse.builder()
					.id(1L)
					.name("헬스 케어")
					.fullName("건강 > 헬스 케어")
					.build())
				.files(List.of(
					FileResponse.builder()
						.id(ID)
						.fileUrl("https://cdn.example.com/chat/image1.jpg")
						.fileName(null)
						.fileSize("20MB")
						.fileType(UploadFileType.IMAGE)
						.build(),
					FileResponse.builder()
						.id(ID)
						.fileUrl("https://cdn.example.com/chat/image1.jpg")
						.fileName(null)
						.fileSize("20MB")
						.fileType(UploadFileType.IMAGE)
						.build()))
				.build())
			.build();

		ChatMessageFindAllResponse messageResponse = ChatMessageFindAllResponse.builder()
			.page(0)
			.hasNext(false)
			.chatMessages(List.of(textMessage, imageMessage, estimateMessage, reviewMessage))
			.build();

		// 최종 응답
		ChatRoomFindDetailResponse response = ChatRoomFindDetailResponse.builder()
			.id(1L)
			.opponent(opponent)
			.request(requestResponse)
			.messageResponse(messageResponse)
			.estimate(ServiceEstimateWithStatusResponse.builder()
				.id(ID)
				.status(ServiceEstimateStatus.ONGOING.name())
				.content(CONTENT_STR)
				.price(500000L)
				.completedAt(formatToDateString(LocalDateTime.now()))
				.scheduledAt(formatToDateString(LocalDateTime.now().plusDays(7)))
				.build())
			.isDirectorPaid(Boolean.TRUE)
			.build();

		given(chatRoomFacade.findDetail(anyLong(), anyLong(), anyLong()))
			.willReturn(response);

		mockMvc.perform(get("/api/chat-rooms/{chatRoomId}", 1L)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.param(LAST_MESSAGE_ID, ZERO_STR)
			)
			.andExpect(status().isOk())
			.andDo(document("chat-room-find-detail-for-public",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("chatRoomId")
						.description("채팅방 ID")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(LAST_MESSAGE_ID)
						.optional()
						.description("마지막 메세지 (없어도 상관없다)")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER).description("채팅방 ID"),
					fieldWithPath("isDirectorPaid").type(JsonFieldType.BOOLEAN).description("디렉터 결제 여부"),

					fieldWithPath("estimate").type(JsonFieldType.OBJECT).description("제안"),
					fieldWithPath("estimate.id").type(JsonFieldType.NUMBER).description("제안 아이디"),
					fieldWithPath("estimate.price").type(JsonFieldType.NUMBER).description("제안 가격"),
					fieldWithPath("estimate.content").type(JsonFieldType.STRING).description("제안 내용"),
					fieldWithPath("estimate.status").type(JsonFieldType.STRING)
						.attributes(enumFormat(ServiceEstimateStatus.class, ServiceEstimateStatus::getDescription))
						.description("제안 상태 (이걸보고 채팅방 안의 오른쪽 상단 상태값 판단)"),
					fieldWithPath("estimate.completedAt").type(JsonFieldType.STRING)
						.optional()
						.attributes(getDateTimeFormat())
						.description("회원이 제안을 완료한 날짜"),
					fieldWithPath("estimate.scheduledAt").type(JsonFieldType.STRING)
						.optional()
						.attributes(getDateTimeFormat())
						.description("회원이 제안을 완료한 날짜"),

					fieldWithPath("opponent").type(JsonFieldType.OBJECT).description("상대방 정보"),
					fieldWithPath("opponent.id").type(JsonFieldType.NUMBER).description("상대방 ID"),
					fieldWithPath("opponent.nickname").type(JsonFieldType.STRING).description("상대방 닉네임"),
					fieldWithPath("opponent.profileImageUrl").type(JsonFieldType.STRING).description("상대방 프로필 이미지 URL"),
					fieldWithPath("opponent.isWithdrawal").type(JsonFieldType.BOOLEAN).description("회원탈퇴 여부"),

					fieldWithPath("request").type(JsonFieldType.OBJECT).description("서비스 요청 정보"),
					fieldWithPath("request.id").type(JsonFieldType.NUMBER).description("서비스 요청 ID"),
					fieldWithPath("request.wishTimes").type(JsonFieldType.ARRAY)
						.description("희망 시간 목록"),
					fieldWithPath("request.wishTimes[].wishTime").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("희망 시간 (yyyy.MM.dd HH:mm)"),

					fieldWithPath("request.service").type(JsonFieldType.OBJECT).description("디렉터 서비스 정보"),
					fieldWithPath("request.service.id").type(JsonFieldType.NUMBER).description("디렉터 서비스 ID"),
					fieldWithPath("request.service.name").type(JsonFieldType.STRING).description("디렉터 서비스 이름"),
					fieldWithPath("request.service.fullName").type(JsonFieldType.STRING)
						.description("디렉터 서비스 전체 경로 이름"),

					fieldWithPath("request.location").type(JsonFieldType.ARRAY).description("위치 목록"),
					fieldWithPath("request.location[].id").type(JsonFieldType.NUMBER).description("위치 ID"),
					fieldWithPath("request.location[].name").type(JsonFieldType.STRING).description("위치 이름"),
					fieldWithPath("request.location[].fullName").type(JsonFieldType.STRING).description("위치 전체 경로 이름"),

					fieldWithPath("messageResponse").type(JsonFieldType.OBJECT).description("메시지 응답 정보"),
					fieldWithPath("messageResponse.page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
					fieldWithPath("messageResponse.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),

					fieldWithPath("messageResponse.chatMessages").type(JsonFieldType.ARRAY).description("채팅 메시지 목록"),
					fieldWithPath("messageResponse.chatMessages[].id").type(JsonFieldType.NUMBER).description("메시지 ID"),
					fieldWithPath("messageResponse.chatMessages[].chatMessageType").type(JsonFieldType.STRING)
						.description("메시지 타입"),
					fieldWithPath("messageResponse.chatMessages[].content").type(JsonFieldType.STRING)
						.optional()
						.description("메시지 내용"),
					fieldWithPath("messageResponse.chatMessages[].isReadByOpponent").type(JsonFieldType.BOOLEAN)
						.description("상대방 읽음 여부 (항상 TRUE 다. 상세조회할때는 무조건 현재까지 있는 모든 메세지를 읽은 상태로 변하기 떄문에)"),
					fieldWithPath("messageResponse.chatMessages[].sendAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("전송 시각"),

					fieldWithPath("messageResponse.chatMessages[].sender").type(JsonFieldType.OBJECT)
						.description("발신자 정보"),
					fieldWithPath("messageResponse.chatMessages[].sender.id").type(JsonFieldType.NUMBER)
						.description("발신자 ID"),
					fieldWithPath("messageResponse.chatMessages[].sender.nickname").type(JsonFieldType.STRING)
						.description("발신자 닉네임"),
					fieldWithPath("messageResponse.chatMessages[].sender.profileImageUrl").type(JsonFieldType.STRING)
						.description("발신자 프로필 이미지 URL"),
					fieldWithPath("messageResponse.chatMessages[].sender.isWithdrawal").type(JsonFieldType.BOOLEAN)
						.description("회원탈퇴 여부"),

					fieldWithPath("messageResponse.chatMessages[].files").type(JsonFieldType.ARRAY)
						.optional()
						.description("파일 목록"),
					fieldWithPath("messageResponse.chatMessages[].files[].id").type(JsonFieldType.NUMBER)
						.optional()
						.description("파일 ID"),
					fieldWithPath("messageResponse.chatMessages[].files[].fileUrl").type(JsonFieldType.STRING)
						.optional()
						.description("파일 URL"),
					fieldWithPath("messageResponse.chatMessages[].files[].fileName").type(JsonFieldType.STRING)
						.optional()
						.description("파일 명"),
					fieldWithPath("messageResponse.chatMessages[].files[].fileType").type(JsonFieldType.STRING)
						.optional()
						.attributes(enumFormat(UploadFileType.class, Enum::name))
						.description("파일 타입"),
					fieldWithPath("messageResponse.chatMessages[].files[].fileSize").type(JsonFieldType.STRING)
						.optional()
						.description("파일 사이즈"),

					fieldWithPath("messageResponse.chatMessages[].estimate").type(JsonFieldType.OBJECT)
						.optional()
						.description("제안 정보"),
					fieldWithPath("messageResponse.chatMessages[].estimate.id").type(JsonFieldType.NUMBER)
						.optional()
						.description("제안 아이디"),
					fieldWithPath("messageResponse.chatMessages[].estimate.title").type(JsonFieldType.STRING)
						.optional()
						.description("제안 제목"),
					fieldWithPath("messageResponse.chatMessages[].estimate.content").type(JsonFieldType.STRING)
						.optional()
						.description("제안 내용"),
					fieldWithPath("messageResponse.chatMessages[].estimate.price").type(JsonFieldType.NUMBER)
						.optional()
						.description("제안 가격"),
					fieldWithPath("messageResponse.chatMessages[].estimate.storeAddress").type(JsonFieldType.STRING)
						.optional()
						.description("제안서 상의 매장 주소"),
					fieldWithPath("messageResponse.chatMessages[].estimate.scheduledAt").type(JsonFieldType.STRING)
						.optional()
						.attributes(getDateTimeFormat())
						.description("서비스 예정일"),

					fieldWithPath("messageResponse.chatMessages[].estimate.files").type(JsonFieldType.ARRAY)
						.optional()
						.description("제안 파일"),
					fieldWithPath("messageResponse.chatMessages[].estimate.files[].id").type(JsonFieldType.NUMBER)
						.optional()
						.description("제안서 파일 ID"),
					fieldWithPath("messageResponse.chatMessages[].estimate.files[].fileUrl").type(JsonFieldType.STRING)
						.optional()
						.description("제안서 파일 이미지 URL"),
					fieldWithPath("messageResponse.chatMessages[].estimate.files[].fileName").type(JsonFieldType.STRING)
						.optional()
						.description("제안서 파일 명"),
					fieldWithPath("messageResponse.chatMessages[].estimate.files[].fileType").type(JsonFieldType.STRING)
						.optional()
						.attributes(enumFormat(UploadFileType.class, Enum::name))
						.description("제안서 파일 타입"),
					fieldWithPath("messageResponse.chatMessages[].estimate.files[].fileSize").type(JsonFieldType.STRING)
						.optional()
						.description("제안서 파일 사이즈"),

					fieldWithPath("messageResponse.chatMessages[].estimate.service").type(JsonFieldType.OBJECT)
						.optional()
						.description("디렉터 서비스 정보"),
					fieldWithPath("messageResponse.chatMessages[].estimate.service.id").type(JsonFieldType.NUMBER)
						.optional()
						.description("디렉터 제안 서비스 ID"),
					fieldWithPath("messageResponse.chatMessages[].estimate.service.name").type(JsonFieldType.STRING)
						.optional()
						.description("디렉터 제안 서비스 이름"),
					fieldWithPath("messageResponse.chatMessages[].estimate.service.fullName").type(JsonFieldType.STRING)
						.optional()
						.description("디렉터 제안 서비스 전체 경로 이름"),

					fieldWithPath("messageResponse.chatMessages[].review").type(JsonFieldType.OBJECT)
						.optional()
						.description("리뷰 정보"),
					fieldWithPath("messageResponse.chatMessages[].review.id").type(JsonFieldType.NUMBER)
						.optional()
						.description("리뷰 아이디"),
					fieldWithPath("messageResponse.chatMessages[].review.title").type(JsonFieldType.STRING)
						.optional()
						.description("리뷰 제목"),
					fieldWithPath("messageResponse.chatMessages[].review.content").type(JsonFieldType.STRING)
						.optional()
						.description("리뷰 내용"),

					fieldWithPath("messageResponse.chatMessages[].review.files").type(JsonFieldType.ARRAY)
						.optional()
						.description("리뷰 파일"),
					fieldWithPath("messageResponse.chatMessages[].review.files[].id").type(JsonFieldType.NUMBER)
						.optional()
						.description("리뷰 파일 ID"),
					fieldWithPath("messageResponse.chatMessages[].review.files[].fileUrl").type(JsonFieldType.STRING)
						.optional()
						.description("리뷰 파일 URL"),
					fieldWithPath("messageResponse.chatMessages[].review.files[].fileName").type(JsonFieldType.STRING)
						.optional()
						.description("리뷰 파일 명"),
					fieldWithPath("messageResponse.chatMessages[].review.files[].fileType").type(JsonFieldType.STRING)
						.optional()
						.attributes(enumFormat(UploadFileType.class, Enum::name))
						.description("리뷰 파일 타입"),
					fieldWithPath("messageResponse.chatMessages[].review.files[].fileSize").type(JsonFieldType.STRING)
						.optional()
						.description("리뷰 파일 사이즈"),

					fieldWithPath("messageResponse.chatMessages[].review.service").type(JsonFieldType.OBJECT)
						.optional()
						.description("리뷰 서비스 정보"),
					fieldWithPath("messageResponse.chatMessages[].review.service.id").type(JsonFieldType.NUMBER)
						.optional()
						.description("리뷰 서비스 ID"),
					fieldWithPath("messageResponse.chatMessages[].review.service.name").type(JsonFieldType.STRING)
						.optional()
						.description("리뷰 서비스 이름"),
					fieldWithPath("messageResponse.chatMessages[].review.service.fullName").type(JsonFieldType.STRING)
						.optional()
						.description("리뷰 서비스 전체 경로 이름")

				),

				resource(builder()
					.tag("💬 채팅방 API")
					.summary("채팅방 상세 조회 (멤버)")
					.description("특정 채팅방의 메시지 목록을 조회합니다. 조회 시 자동으로 읽음 처리됩니다.")
					.build())
			));
	}

	@Test
	void 채팅방_삭제() throws Exception {
		authenticationSetUp();

		doNothing().when(chatRoomFacade).delete(anyLong(), anyLong());

		mockMvc.perform(delete("/api/chat-rooms/{chatRoomId}", 1L)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isNoContent())
			.andDo(document("chat-room-delete-for-public",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("chatRoomId")
						.description("삭제할 채팅방 ID")
				),

				resource(builder()
					.tag("💬 채팅방 API")
					.summary("채팅방 나가기 (멤버)")
					.description("특정 채팅방에서 나갑니다. 양쪽 멤버 모두 나가면 채팅방이 삭제됩니다.")
					.build())
			));
	}

	@Test
	void 채팅방_읽지_않은_메시지_총_개수_조회() throws Exception {
		authenticationSetUp();

		ChatRoomUnreadCountResponse response = ChatRoomUnreadCountResponse.builder()
			.totalUnreadCount(15)
			.build();

		given(chatRoomFacade.countTotalUnreadMessagesForPublic(anyLong()))
			.willReturn(response);

		mockMvc.perform(get("/api/chat-rooms/unread-count")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("chat-room-find-total-unread-count-for-public",
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
					.summary("읽지 않은 메시지 총 개수 조회 (멤버)")
					.description("멤버(또는 디렉터)가 참여한 모든 채팅방의 읽지 않은 메시지 총 개수를 조회합니다.")
					.build())
			));
	}

}
