package com.motd.be.common.filter.security_filter;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.log.LogUtils.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.AuthException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	//여기에 오는건 인증되지 않은 사용자가 인증되어야 접근할수 있는 리소스에 접근할때 발생한다.
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException {
		// 상태 코드
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType("application/json;charset=UTF-8");

		// body JSON (ResponseEntity 바디와 동일한 구조)
		Map<String, String> body = Map.of(
			ERROR_STATUS, AuthException.UNAUTHORIZE_IN_SECURITY_FILTER.getHttpStatus().toString(),
			ERROR_MESSAGE, AuthException.UNAUTHORIZE_IN_SECURITY_FILTER.getErrorMessage(),
			ERROR_CODE, AuthException.UNAUTHORIZE_IN_SECURITY_FILTER.getCode()
		);

		loggingFilterLevelCustomException(new CustomRuntimeException(AuthException.UNAUTHORIZE_IN_SECURITY_FILTER));

		// Jackson으로 직렬화
		ObjectMapper mapper = new ObjectMapper();
		response.getWriter().write(mapper.writeValueAsString(body));
	}

	private void loggingFilterLevelCustomException(
		CustomRuntimeException e
	) {
		Map<String, String> logDetails = new LinkedHashMap<>();
		logDetails.put(REQUEST_LOGGING_ID, MDC.get(REQUEST_LOGGING_ID));
		logDetails.put(REQUEST_URL, MDC.get(REQUEST_URL));
		logDetails.put(HTTP_METHOD, MDC.get(HTTP_METHOD));
		logDetails.put(ERROR_CODE, e.getCustomException().getCode());
		logDetails.put(ERROR_MESSAGE, e.getMessage());

		log.warn(
			buildPrettyLogBlock("FILTER_CUSTOM_EXCEPTION", logDetails)
		);
	}
}
