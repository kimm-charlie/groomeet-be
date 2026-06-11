package com.motd.be.rest_docs.module.member.sse;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.Constants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.member.entity.Role;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class SseControllerRestDocsTest extends BaseRestDocsTest {

	@Test
	void SSE_연결() throws Exception {
		authenticationSetUp();

		SseEmitter emitter = new SseEmitter(60000L);

		given(sseFacade.connect(anyLong(), any(Role.class), anyString())).willReturn(emitter);

		mockMvc.perform(get("/api/sse/connect")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.param("role", Role.MEMBER.name())
				.param(LAST_EVENT_ID, "0")
				.accept(MediaType.TEXT_EVENT_STREAM_VALUE))
			.andExpect(status().isOk())
			.andDo(document("sse-connect",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("role")
						.description("현재 활성화된 클라이언트 역할 (MEMBER 또는 DIRECTOR)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(LAST_EVENT_ID)
						.optional()
						.description("마지막 받은 이벤트 아이디 (따로 안보내줘도 클라이언트가 알아서 보내줌)")
				),

				resource(builder()
					.tag("🔔 SSE (Server-Sent Events) API")
					.summary("SSE 연결")
					.description("클라이언트가 서버와 SSE 연결을 수립합니다. \n" +
						"- 서버는 실시간 이벤트(채팅방 refresh)를 클라이언트에게 전송할 수 있습니다. \n" +
						"- 연결은 3분 타임아웃이 설정되어 있습니다. \n" +
						"- 같은 회원이 여러 개의 연결을 맺을 수 있습니다. \n" +
						"- 해당 페이지에서 나가면 close 를 클라이언트 측에서 호출해줘야 합니다. 모르면 물어보세요.")
					.build())
			));
	}
}
