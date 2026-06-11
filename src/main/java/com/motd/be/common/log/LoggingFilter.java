package com.motd.be.common.log;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.filter.util.JwtTokenUtils.*;
import static com.motd.be.common.log.LogUtils.*;
import static org.springframework.http.MediaType.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.ErrorResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingFilter extends OncePerRequestFilter {

	private final ObjectMapper objectMapper;

	public LoggingFilter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
		HttpServletResponse response,
		FilterChain chain)
		throws IOException, ServletException {

		String requestLoggingId = UUID.randomUUID().toString();
		request.setAttribute(REQUEST_LOGGING_ID, requestLoggingId);
		MDC.put(REQUEST_LOGGING_ID, requestLoggingId);

		try {
			preProcessLogging(request, requestLoggingId);

			// SSE 요청
			String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
			if (acceptHeader != null && acceptHeader.contains(TEXT_EVENT_STREAM_VALUE)) {
				MDC.put(REQUEST_BODY, "sse-request");
				chain.doFilter(request, response);
				return;
			}

			// health check
			if (request.getRequestURI().contains(HEALTH_CHECK_PATH)) {
				chain.doFilter(request, response);
				return;
			}

			ContentCachingRequestWrapper cachedRequest = new ContentCachingRequestWrapper(request);
			ContentCachingResponseWrapper cachedResponse = new ContentCachingResponseWrapper(response);

			try {
				chain.doFilter(cachedRequest, cachedResponse);
			} catch (CustomRuntimeException e) {
				sendCustomRuntimeExceptionResponse(cachedResponse, e);
				loggingFilterLevelCustomException(e, request);
			} catch (Exception e) {
				sendUnExpectedExceptionResponse(cachedResponse, e);
				loggingUnexpectedError(e, request);
			} finally {
				postProcessLoggingSafe(cachedRequest, cachedResponse);
			}

		} finally {
			MDC.clear();
		}
	}

	private void postProcessLoggingSafe(HttpServletRequest request,
		HttpServletResponse response) throws
		IOException {
		if (request instanceof ContentCachingRequestWrapper cachedRequest &&
			response instanceof ContentCachingResponseWrapper cachedResponse) {
			postProcessLogging(cachedRequest, cachedResponse);
		}
	}

	private void sendUnExpectedExceptionResponse(HttpServletResponse response, Exception e) throws
		IOException {
		response.setCharacterEncoding(UTF_8);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

		ErrorResponse errorResponse = ErrorResponse.fromUnexpected(e);
		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}

	private void sendCustomRuntimeExceptionResponse(HttpServletResponse response, CustomRuntimeException e) throws
		IOException {
		response.setCharacterEncoding(UTF_8);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(e.getCustomException().getHttpStatus().value());

		ErrorResponse errorResponse = ErrorResponse.from(e.getCustomException());
		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}

	private void preProcessLogging(HttpServletRequest cachedRequest, String requestLoggingId) {

		String requestUrl = getUrlContainingQueryString(cachedRequest);
		String id = extractMemberIdFromAccessTokenWithCatchJwtException(cachedRequest);

		MDC.put(ID, id);
		MDC.put(REQUEST_LOGGING_ID, requestLoggingId);
		MDC.put(REQUEST_URL, requestUrl);
		MDC.put(HTTP_METHOD, cachedRequest.getMethod());
		MDC.put(X_FORWARDED_FOR_WITHOUT_UNDER_BAR, cachedRequest.getHeader(X_FORWARDED_FOR));
		MDC.put(X_REAL_IP_WITHOUT_UNDER_BAR, cachedRequest.getHeader(X_REAL_IP));
	}

	/**
	 * filter 안에서 처리하지 못한 예외가 발생한경우 requestBody 를 찍는다.
	 * 이때 HttpServletRequest 를 사용하는 이유는 아직 requestBody 가 읽히지 않았기때문에 ContentCachingRequestWrapper 에는 requestBody 가 저장되어있지않다.
	 * 의도: dispatcher servlet 이전단에서 발생하는 예외에 대한 Logging 을 하는 메서드
	 */

	private void loggingUnexpectedError(Exception e, HttpServletRequest request) {

		String requestBody = getRequestBodyFromHttpServletRequest(request);
		MDC.put(REQUEST_BODY, requestBody);

		Map<String, String> logDetails = new LinkedHashMap<>();
		logDetails.put(REQUEST_LOGGING_ID, MDC.get(REQUEST_LOGGING_ID));
		logDetails.put(REQUEST_URL, MDC.get(REQUEST_URL));
		logDetails.put(HTTP_METHOD, MDC.get(HTTP_METHOD));
		logDetails.put(REQUEST_BODY, requestBody);
		logDetails.put(CAUSE, String.valueOf(e.getCause()));
		logDetails.put(TRACE, getStackTrace(e));

		log.error(buildPrettyLogBlock("UNHANDLED_EXCEPTION", logDetails), e);
	}

	/**
	 * Filter 단계에서 발생한 CustomRuntimeException에 대한 로깅
	 * <p>
	 * - DispatcherServlet 이전 단계에서 발생하여 ControllerAdvice를 타지 못한 경우를 보완
	 * - 비즈니스 예외이므로 ERROR가 아닌 WARN 레벨로 기록
	 */
	private void loggingFilterLevelCustomException(
		CustomRuntimeException e,
		HttpServletRequest request
	) {

		String requestBody = getRequestBodyFromHttpServletRequest(request);

		MDC.put(REQUEST_BODY, requestBody);

		Map<String, String> logDetails = new LinkedHashMap<>();
		logDetails.put(REQUEST_LOGGING_ID, MDC.get(REQUEST_LOGGING_ID));
		logDetails.put(REQUEST_URL, MDC.get(REQUEST_URL));
		logDetails.put(HTTP_METHOD, MDC.get(HTTP_METHOD));
		logDetails.put(REQUEST_BODY, requestBody);
		logDetails.put(ERROR_CODE, e.getCustomException().getCode());
		logDetails.put(ERROR_MESSAGE, e.getMessage());

		log.warn(
			buildPrettyLogBlock("FILTER_CUSTOM_EXCEPTION", logDetails)
		);
	}

	private void postProcessLogging(ContentCachingRequestWrapper cachedRequest,
		ContentCachingResponseWrapper cachedResponse) throws IOException {

		// 응답 본문을 바이트 배열로 얻기
		// 바이트 배열을 문자열로 변환 (응답의 캐릭터셋을 고려하여 변환)
		MDC.put(RESPONSE_STATUS, String.valueOf(cachedResponse.getStatus()));
		cachedResponse.copyBodyToResponse();

		if (cachedRequest.getRequestURI().equals(PROMETHEUS_URI)) {
			return;
		}

		if (log.isDebugEnabled()) {
			Map<String, String> logDetails = new LinkedHashMap<>();
			logDetails.put(REQUEST_LOGGING_ID, MDC.get(REQUEST_LOGGING_ID));
			logDetails.put(REQUEST_URL, MDC.get(REQUEST_URL));
			logDetails.put(HTTP_METHOD, MDC.get(HTTP_METHOD));
			logDetails.put(REQUEST_BODY,
				getRequestBodyFromContentCachingRequestWrapper(cachedRequest));
			logDetails.put(RESPONSE_STATUS, MDC.get(RESPONSE_STATUS));

			log.info(buildPrettyLogBlock(REQUEST, logDetails));
		} else {
			// 이게 있어야 실제 prod 서버에서 requestLoggingId 가 찍히면서, 요청에대한 후처리 로그가 info 레벨로 남겨진다.
			log.info(REQUEST_LOGGING_ID + ": {}", MDC.get(REQUEST_LOGGING_ID));
		}
	}
}
