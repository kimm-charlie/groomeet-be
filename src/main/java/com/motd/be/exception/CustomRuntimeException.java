package com.motd.be.exception;

import static com.motd.be.common.constants.Constants.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import lombok.Getter;

@Getter
public class CustomRuntimeException extends RuntimeException {

	private final CustomException customException;
	private final String customMessage;

	public CustomRuntimeException(CustomException customException) {
		this.customException = customException;
		this.customMessage = null;
	}

	public CustomRuntimeException(CustomException customException, String customMessage) {
		this.customException = customException;
		this.customMessage = customMessage;
	}

	public ResponseEntity<Map<String, String>> sendError() {
		Map<String, String> responseMap = new LinkedHashMap<>();
		responseMap.put(ERROR_STATUS, customException.getHttpStatus().toString());
		responseMap.put(ERROR_MESSAGE, getEffectiveMessage());
		responseMap.put(ERROR_CODE, customException.getCode());
		return ResponseEntity.status(customException.getHttpStatus()).body(responseMap);
	}

	public String getMessage() {
		return getEffectiveMessage();
	}

	private String getEffectiveMessage() {
		return customMessage != null ? customMessage : customException.getErrorMessage();
	}
}
