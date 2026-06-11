package com.motd.be.exception.handler;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.log.LogUtils.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.HandlerException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
@Order(1)
@RequiredArgsConstructor
public class CustomExceptionHandler {

	@ExceptionHandler(CustomRuntimeException.class)
	public ResponseEntity<Map<String, String>> customExceptionHandler(CustomRuntimeException e) {
		Map<String, String> exceptionAdditionalInfo = Map.of(ERROR_TYPE, e.getCustomException().getName(),
			ERROR_MESSAGE, e.getCustomException().getErrorMessage());
		logging(exceptionAdditionalInfo);
		return e.sendError();
	}

	/**
	 * @param e
	 * @return
	 * @RequestBody 가 비어있을 때 발생하는 예외이다.
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException e) {
		Map<String, String> exceptionAdditionalInfo = Map.of(
			ERROR_TYPE, e.getClass().getSimpleName(),
			ERROR_MESSAGE, HandlerException.REQUEST_BODY_MISSING.getErrorMessage()
		);

		logging(exceptionAdditionalInfo);

		return ResponseEntity
			.status(HandlerException.REQUEST_BODY_MISSING.getHttpStatus())
			.body(Map.of(
				ERROR_STATUS, HandlerException.REQUEST_BODY_MISSING.getHttpStatus().toString(),
				ERROR_MESSAGE, HandlerException.REQUEST_BODY_MISSING.getErrorMessage(),
				ERROR_CODE, HandlerException.REQUEST_BODY_MISSING.getCode()
			));
	}

	/**
	 * @param e
	 * @return
	 * @PreAuthorize 어노테이션에서 권한이 없을때 발생하는 예외이다.
	 */
	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ResponseEntity<Map<String, String>> handleUnauthorizedException(AccessDeniedException e) {
		Map<String, String> exceptionAdditionalInfo = Map.of(
			ERROR_TYPE, e.getClass().getSimpleName(),
			ERROR_MESSAGE, HandlerException.FORBIDDEN.getErrorMessage()
		);

		logging(exceptionAdditionalInfo);

		return ResponseEntity.status(HandlerException.FORBIDDEN.getHttpStatus())
			.body(Map.of(
				ERROR_STATUS, HandlerException.FORBIDDEN.getHttpStatus().toString(),
				ERROR_MESSAGE, HandlerException.FORBIDDEN.getErrorMessage(),
				ERROR_CODE, HandlerException.FORBIDDEN.getCode()
			));
	}

	/**
	 * 특정 url 에 정의되지 않은 HttpMethod 를 사용한 요청을 하면 발생하는 예외 이다.
	 *
	 * @param e
	 * @return
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<Map<String, String>> handleInvalidHttpMethodException(
		HttpRequestMethodNotSupportedException e) {
		Map<String, String> exceptionAdditionalInfo = Map.of(
			ERROR_TYPE, e.getClass().getSimpleName(),
			ERROR_MESSAGE, HandlerException.INVALID_HTTP_METHOD.getErrorMessage()
		);

		logging(exceptionAdditionalInfo);

		return ResponseEntity.status(HandlerException.INVALID_HTTP_METHOD.getHttpStatus())
			.body(Map.of(
				ERROR_STATUS, HandlerException.INVALID_HTTP_METHOD.getHttpStatus().toString(),
				ERROR_MESSAGE, HandlerException.INVALID_HTTP_METHOD.getErrorMessage(),
				ERROR_CODE, HandlerException.INVALID_HTTP_METHOD.getCode()
			));
	}

	/**
	 * @param e
	 * @return controller 에서 url 에 쿼리파라미터가 존재하지 않을때 발생하는 오류를 잡는다.
	 */
	@ExceptionHandler({UnsatisfiedServletRequestParameterException.class,
		MissingServletRequestParameterException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<Map<String, String>> handleUnsatisfiedServletRequestParameterException(Exception e) {
		Map<String, String> exceptionAdditionalInfo = Map.of(
			ERROR_TYPE, e.getClass().getSimpleName(),
			ERROR_MESSAGE, HandlerException.UNSATISFIED_PARAMETER.getErrorMessage()
		);

		logging(exceptionAdditionalInfo);

		return ResponseEntity.status(HandlerException.UNSATISFIED_PARAMETER.getHttpStatus())
			.body(Map.of(
				ERROR_STATUS, HandlerException.UNSATISFIED_PARAMETER.getHttpStatus().toString(),
				ERROR_MESSAGE, HandlerException.UNSATISFIED_PARAMETER.getErrorMessage(),
				ERROR_CODE, HandlerException.UNSATISFIED_PARAMETER.getCode()
			));
	}

	/**
	 * MaxUploadSizeExceededException.class 예외는 Dispatcher Servlet 이후 요청 파싱 단계에서 발생한다.
	 * 즉 Controller 호출 전에 발생하는 예외이기 때문에 MaxUploadSizeExceededException 을 GlobalExceptionHandler 에서 잡는다.
	 */
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<Map<String, String>> maxUploadSizeExceededException(MaxUploadSizeExceededException e) {
		Map<String, String> exceptionAdditionalInfo = Map.of(
			ERROR_TYPE, e.getClass().getSimpleName(),
			ERROR_MESSAGE, HandlerException.MAX_UPLOAD_SIZE.getErrorMessage()
		);

		logging(exceptionAdditionalInfo);

		return ResponseEntity.status(HandlerException.MAX_UPLOAD_SIZE.getHttpStatus())
			.body(Map.of(
				ERROR_STATUS, HandlerException.MAX_UPLOAD_SIZE.getHttpStatus().toString(),
				ERROR_MESSAGE, HandlerException.MAX_UPLOAD_SIZE.getErrorMessage(),
				ERROR_CODE, HandlerException.MAX_UPLOAD_SIZE.getCode()
			));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException e) {
		Map<String, String> exceptionAdditionalInfo = Map.of(
			ERROR_TYPE, e.getClass().getSimpleName(),
			ERROR_MESSAGE, Objects.requireNonNull(e.getBindingResult().getAllErrors().get(0).getDefaultMessage())
		);

		logging(exceptionAdditionalInfo);

		return ResponseEntity.status(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus())
			.body(Map.of(
				ERROR_STATUS, HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString(),
				ERROR_MESSAGE, Objects.requireNonNull(e.getBindingResult().getAllErrors().get(0).getDefaultMessage()),
				ERROR_CODE, HandlerException.ARGUMENT_NOT_VALID.getCode()
			));
	}

	@ExceptionHandler(MissingServletRequestPartException.class)
	public ResponseEntity<Map<String, String>> handleMissingServletRequestPartException(
		MissingServletRequestPartException e) {
		Map<String, String> exceptionAdditionalInfo = Map.of(
			ERROR_TYPE, e.getClass().getSimpleName(),
			ERROR_MESSAGE, Objects.requireNonNull(e.getMessage())
		);

		logging(exceptionAdditionalInfo);

		return ResponseEntity.status(HandlerException.MISSING_REQUEST_PART.getHttpStatus())
			.body(Map.of(
				ERROR_STATUS, HandlerException.MISSING_REQUEST_PART.getHttpStatus().toString(),
				ERROR_MESSAGE, Objects.requireNonNull(e.getMessage()),
				ERROR_CODE, HandlerException.MISSING_REQUEST_PART.getErrorMessage()
			));
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public void noHandlerFoundException(HttpServletResponse response, NoHandlerFoundException e) throws IOException {
		Map<String, String> exceptionAdditionalInfo = Map.of(
			ERROR_TYPE, e.getClass().getSimpleName(),
			ERROR_MESSAGE, e.getMessage()
		);

		logging(exceptionAdditionalInfo);

		response.sendRedirect("/api/docs/not-found");
	}

	private void logging(Map<String, String> exceptionAdditionalInfo) {
		HttpServletRequest request = ((ServletRequestAttributes)Objects.requireNonNull(
			RequestContextHolder.getRequestAttributes())).getRequest();

		HttpServletRequest cachedRequest = unwrapRequest(request);

		if (cachedRequest instanceof ContentCachingRequestWrapper wrapper) {
			MDC.put(REQUEST_BODY, getRequestBodyFromContentCachingRequestWrapper(wrapper));
		} else {
			if (MDC.get(REQUEST_BODY) == null) {
				MDC.put(REQUEST_BODY, "non-cached-request");
			}
		}
		MDC.put(ERROR_TYPE, exceptionAdditionalInfo.get(ERROR_TYPE));
		MDC.put(ERROR_MESSAGE, exceptionAdditionalInfo.get(ERROR_MESSAGE));

		Map<String, String> logDetails = new LinkedHashMap<>();
		logDetails.put(REQUEST_LOGGING_ID, MDC.get(REQUEST_LOGGING_ID));
		logDetails.put(REQUEST_URL, MDC.get(REQUEST_URL));
		logDetails.put(HTTP_METHOD, MDC.get(HTTP_METHOD));
		logDetails.put(RESPONSE_STATUS, MDC.get(RESPONSE_STATUS));
		logDetails.put(REQUEST_BODY, MDC.get(REQUEST_BODY));
		logDetails.put(ERROR_TYPE, MDC.get(ERROR_TYPE));
		logDetails.put(ERROR_MESSAGE, MDC.get(ERROR_MESSAGE));

		log.warn(buildPrettyLogBlock(HANDLED_EXCEPTION, logDetails));
	}
}
