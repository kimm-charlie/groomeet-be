package com.motd.be.common.interceptor;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.filter.util.JwtTokenUtils.*;
import static com.motd.be.module.member.jwt.JwtProvider.*;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CookieAuthHandshakeInterceptor implements HandshakeInterceptor {

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Map<String, Object> attributes) {

		if (request instanceof ServletServerHttpRequest servletRequest) {
			HttpServletRequest httpRequest = servletRequest.getServletRequest();

			// 쿠키에서 accessToken 추출
			String accessToken = extractAccessToken(httpRequest);

			// JWT 파싱
			Claims claims = getClaimsFromAccessToken(accessToken);
			Long memberId = claims.get(ID, Long.class);
			String role = claims.get(ROLE, String.class);

			// WebSocketSession 으로 전달할 값 저장
			attributes.put(ID, memberId);
			attributes.put(ROLE, role);
		}

		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Exception exception) {
		// no-op
	}
}
