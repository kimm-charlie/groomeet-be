package com.motd.be.rest_docs.module.director.notification;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.rest_docs.Utils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.request.RequestDocumentation;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.director.notification.dto.response.NotificationExistResponseForDirector;
import com.motd.be.module.director.notification.dto.response.NotificationFindAllResponseForDirector;
import com.motd.be.module.director.notification.dto.response.NotificationResponseForDirector;
import com.motd.be.module.member.notification.entity.NotificationCategoryType;
import com.motd.be.module.member.notification.entity.NotificationType;
import com.motd.be.module.member.notification.entity.ReferenceType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class NotificationForDirectorRestDocsTest extends BaseRestDocsTest {

	@Test
	void 디렉터_알림_전체_조회() throws Exception {
		authenticationSetUp();

		NotificationFindAllResponseForDirector response = NotificationFindAllResponseForDirector.builder()
			.page(0)
			.hasNext(true)
			.notifications(List.of(
				NotificationResponseForDirector.builder()
					.id(1L)
					.type(NotificationType.REVIEW_WRITTEN.name())
					.title(NotificationType.REVIEW_WRITTEN.getTitle())
					.content("(사용자명)" + NotificationType.REVIEW_WRITTEN.getDescription())
					.referenceId(10L)
					.referenceType(ReferenceType.SERVICE_ESTIMATE.name())
					.isRead(Boolean.FALSE)
					.createdAt(LocalDateTime.now())
					.senderId(100L)
					.build(),
				NotificationResponseForDirector.builder()
					.id(2L)
					.type(NotificationType.TRANSACTION_CONFIRMED.name())
					.title(NotificationType.TRANSACTION_CONFIRMED.getTitle())
					.content("(사용자명)" + NotificationType.TRANSACTION_CONFIRMED.getDescription())
					.referenceId(20L)
					.referenceType(ReferenceType.CHAT_ROOM.name())
					.isRead(Boolean.TRUE)
					.createdAt(LocalDateTime.now().minusDays(1))
					.senderId(200L)
					.build()))
			.build();

		given(notificationFacadeForDirector.findAllForDirector(anyLong(), anyInt(), anyString())).willReturn(response);

		mockMvc.perform(get("/api/directors/notifications")
				.param(PAGE_STR, ZERO_STR)
				.param(NOTIFICATION_CATEGORY_TYPE, NotificationCategoryType.TRANSACTION.name())
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("notification-director-findAll",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(PAGE_STR)
						.optional()
						.description("페이지 번호 (0부터 시작)"),
					RequestDocumentation.parameterWithName(NOTIFICATION_CATEGORY_TYPE)
						.attributes(enumFormat(NotificationCategoryType.class, NotificationCategoryType::name))
						.optional()
						.description("카테고리 타입")
				),

				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
					fieldWithPath("notifications").type(JsonFieldType.ARRAY).description("알림 목록"),
					fieldWithPath("notifications[].id").type(JsonFieldType.NUMBER).description("알림 ID"),
					fieldWithPath("notifications[].type").type(JsonFieldType.STRING).description("알림 유형"),
					fieldWithPath("notifications[].title").type(JsonFieldType.STRING)
						.attributes(enumFormat(NotificationType.class, NotificationType::getTitle))
						.description("알림 제목"),
					fieldWithPath("notifications[].content").type(JsonFieldType.STRING)
						.attributes(enumFormat(NotificationType.class, NotificationType::getDescription))
						.description("알림 내용"),
					fieldWithPath("notifications[].referenceId").type(JsonFieldType.NUMBER)
						.description("연관 리소스 ID"),
					fieldWithPath("notifications[].referenceType").type(JsonFieldType.STRING)
						.attributes(enumFormat(ReferenceType.class, Enum::name))
						.description("연관 리소스 타입"),
					fieldWithPath("notifications[].isRead").type(JsonFieldType.BOOLEAN)
						.description("읽음 여부"),
					fieldWithPath("notifications[].createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("생성일시"),
					fieldWithPath("notifications[].senderId").type(JsonFieldType.NUMBER)
						.optional()
						.description("발신자 회원 ID")
				),

				resource(builder()
					.tag("🔔 알림 API")
					.summary("디렉터 알림 목록 조회")
					.description("디렉터 알림을 페이지네이션으로 조회합니다.")
					.build())
			));
	}

	@Test
	void 디렉터_알림_존재_여부_확인() throws Exception {
		authenticationSetUp();

		NotificationExistResponseForDirector response = NotificationExistResponseForDirector.of(Boolean.FALSE);

		given(notificationFacadeForDirector.hasUnreadForDirector(anyLong())).willReturn(response);

		mockMvc.perform(get("/api/directors/notifications/exists")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("notification-director-exists",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("hasUnreadNotification").type(JsonFieldType.BOOLEAN)
						.description("읽지 않은 알림 존재 여부")
				),

				resource(builder()
					.tag("🔔 알림 API")
					.summary("디렉터 알림 여부 확인")
					.description("디렉터에게 읽지 않은 알림이 존재하는지 확인합니다.")
					.build())
			));
	}
}
