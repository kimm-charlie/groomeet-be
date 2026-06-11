package com.motd.be.exception.handler;

import java.nio.charset.StandardCharsets;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import com.motd.be.common.constants.Constants;
import com.motd.be.exception.CustomException;
import com.motd.be.exception.CustomRuntimeException;

import io.micrometer.common.lang.Nullable;

@Component
public class CustomStompErrorHandler extends StompSubProtocolErrorHandler {

	/**
	 * 예외 정보를 기반으로 JSON 형태의 에러 페이로드를 생성합니다.
	 */
	private static String buildErrorPayload(Throwable ex) {
		String code = Constants.INTERNAL_ERROR;
		String errorType = ex.getClass().getSimpleName();
		String message = ex.getMessage();

		if (ex.getCause() instanceof CustomRuntimeException cre) {
			CustomException customException = cre.getCustomException();
			code = customException.getCode();
			message = customException.getErrorMessage();
			errorType = customException.getClass().getSimpleName();
		} else if (ex.getCause() instanceof RuntimeException re) {
			code = Constants.RUNTIME_ERROR;
			message = re.getMessage();
			errorType = re.getClass().getSimpleName();
		}

		return """
			{
			  "code": "%s",
			  "errorType": "%s",
			  "message": "%s"
			}
			""".formatted(code, errorType, message);
	}

	@Override
	@Nullable
	public Message<byte[]> handleClientMessageProcessingError(
		@Nullable Message<byte[]> clientMessage, Throwable ex) {

		// 1. STOMP ERROR 프레임 헤더 생성
		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
		accessor.setLeaveMutable(true);

		// 세션 정보 설정
		if (clientMessage != null) {
			StompHeaderAccessor clientHeaders = StompHeaderAccessor.wrap(clientMessage);
			accessor.setSessionId(clientHeaders.getSessionId());
		}

		// 2. 에러 페이로드 생성
		String payload = buildErrorPayload(ex);

		// 3. Message 생성 후 반환
		return handleInternal(accessor, payload.getBytes(StandardCharsets.UTF_8), null, null);
	}

	@Override
	protected Message<byte[]> handleInternal(StompHeaderAccessor errorHeaderAccessor, byte[] errorPayload,
		@Nullable Throwable cause, @Nullable StompHeaderAccessor clientHeaderAccessor) {

		return MessageBuilder.createMessage(errorPayload, errorHeaderAccessor.getMessageHeaders());
	}

}
