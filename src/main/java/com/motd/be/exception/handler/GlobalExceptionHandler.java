package com.motd.be.exception.handler;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.log.LogUtils.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
@Order(2)
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	// 1. AsyncRequestTimeoutException 전용 핸들러 (응답 없음)
	// Spring의 비동기 요청 타임아웃 예외를 잡고 아무것도 반환하지 않음
	@ExceptionHandler(AsyncRequestTimeoutException.class)
	public void handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex) {
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> errorHandler(Exception e) {
		//

		HttpServletRequest request = ((ServletRequestAttributes)Objects.requireNonNull(
			RequestContextHolder.getRequestAttributes())).getRequest();

		HttpServletRequest cachedRequest = unwrapRequest(request);

		MDC.put(ERROR_MESSAGE, String.valueOf(e.getMessage()));
		MDC.put(CAUSE, String.valueOf(e.getCause()));
		MDC.put(TRACE, Arrays.toString(e.getStackTrace()));
		if (cachedRequest instanceof ContentCachingRequestWrapper wrapper) {
			MDC.put(REQUEST_BODY, getRequestBodyFromContentCachingRequestWrapper(wrapper));
		} else {
			MDC.put(REQUEST_BODY, "non-cached-request");
		}

		Map<String, String> logDetails = new LinkedHashMap<>();
		logDetails.put(REQUEST_LOGGING_ID, MDC.get(REQUEST_LOGGING_ID));
		logDetails.put(REQUEST_URL, MDC.get(REQUEST_URL));
		logDetails.put(HTTP_METHOD, MDC.get(HTTP_METHOD));
		logDetails.put(RESPONSE_STATUS, MDC.get(RESPONSE_STATUS));
		logDetails.put(REQUEST_BODY, MDC.get(REQUEST_BODY));
		logDetails.put(ERROR_MESSAGE, MDC.get(ERROR_MESSAGE));
		logDetails.put(CAUSE, MDC.get(CAUSE));
		logDetails.put(TRACE, getStackTrace(e));

		log.error(buildPrettyLogBlock(UNHANDLED_EXCEPTION, logDetails), e);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(Collections.singletonMap(ERROR_MESSAGE, String.valueOf(e.getMessage())));
	}
}
