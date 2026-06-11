package com.motd.be.exception.handler;

import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;

import com.motd.be.exception.CustomException;
import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.ErrorResponse;
import com.motd.be.exception.exceptions.HandlerException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class CustomStompMessageExceptionHandler {

	@MessageExceptionHandler(CustomRuntimeException.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ErrorResponse normalChatExceptionHandler(CustomRuntimeException e) {
		CustomException custom = e.getCustomException();
		return new ErrorResponse(
			custom.getCode(),
			custom.getClass().getSimpleName(),
			custom.getErrorMessage()
		);
	}

	@MessageExceptionHandler(MessageConversionException.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public ErrorResponse handleMessageConversionException(MessageConversionException e) {

		Throwable cause = e.getCause();
		if (cause instanceof MethodArgumentNotValidException manv) {
			String msg = manv.getBindingResult().getAllErrors().get(0).getDefaultMessage();
			return new ErrorResponse(
				HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString(),
				msg,
				HandlerException.ARGUMENT_NOT_VALID.getCode()
			);
		}

		return new ErrorResponse(
			"400",
			"메시지 파싱 중 오류가 발생했습니다.",
			"MESSAGE_CONVERSION_ERROR"
		);
	}
}
