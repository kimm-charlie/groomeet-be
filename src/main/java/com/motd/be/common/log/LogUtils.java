package com.motd.be.common.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class LogUtils {

	public static String getRequestBodyFromHttpServletRequest(HttpServletRequest cachedRequest) {
		StringBuilder requestBody = new StringBuilder();
		String line;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(cachedRequest.getInputStream()))) {
			while ((line = reader.readLine()) != null) {
				requestBody.append(line).append("\n");
			}
		} catch (IOException ex) {
			return null;
		}
		return requestBody.toString();
	}

	public static String getRequestBodyFromContentCachingRequestWrapper(ContentCachingRequestWrapper cachedRequest) {
		if (cachedRequest != null) {
			byte[] buf = cachedRequest.getContentAsByteArray();
			if (buf.length > 0) {
				return new String(buf, 0, buf.length, StandardCharsets.UTF_8);
			}
		}
		return " - ";
	}

	public static String getUrlContainingQueryString(HttpServletRequest cachedRequest) {
		String decodedQueryString = Optional.ofNullable(cachedRequest.getQueryString())
			.map(qs -> URLDecoder.decode(qs, StandardCharsets.UTF_8))
			.orElse("");

		return cachedRequest.getRequestURI() + (!decodedQueryString.isEmpty() ? "?" + decodedQueryString : "");
	}

	/**
	 * dispathcer servlet 이후 단에선 HttpServeltRequest 가 spring security 에 의해 다른 객체로 래핑된다.
	 * 따라서 request body 를 꺼내기위해 securty 관련 request 로 래핑된 객체에서 contentCachingRequestWrapper 를 찾는 메서드 이다.
	 *
	 * @param request
	 * @return
	 */
	public static HttpServletRequest unwrapRequest(HttpServletRequest request) {
		if (request instanceof ContentCachingRequestWrapper) {
			// 이미 ContentCachingRequestWrapper 타입이면 바로 반환
			return request;
		} else if (request instanceof HttpServletRequestWrapper) {
			// 다른 타입의 래퍼라면, 내부의 원본 요청 객체를 추출
			HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper)request;
			HttpServletRequest wrappedRequest = (HttpServletRequest)wrapper.getRequest();
			return unwrapRequest(wrappedRequest); // 재귀적으로 내부 요청 객체 추출
		} else {
			// 더 이상 감싸진 객체가 없거나 원하는 타입이 아니면 반환
			return request;
		}
	}

	public static StringBuilder logRequestLoggingIdInStringBuilder(StringBuilder sb) {
		return sb.append(MDC.get("requestLoggingId"));
	}

	public static String buildPrettyLogBlock(String title, Map<String, String> details) {
		StringBuilder builder = new StringBuilder()
			.append(System.lineSeparator())
			.append("======== ").append(title).append(" ========")
			.append(System.lineSeparator());

		details.forEach((key, value) -> builder
			.append(key)
			.append(": ")
			.append(Optional.ofNullable(value).orElse("-"))
			.append(System.lineSeparator()));

		builder.append("==============================");
		return builder.toString();
	}

	public static String getStackTrace(Throwable throwable) {
		if (throwable == null) {
			return "-";
		}

		StringWriter stringWriter = new StringWriter();
		throwable.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}

}
