package com.motd.be.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

	private String status;
	private String message;
	private String code;

	public static ErrorResponse from(CustomException customException) {
		return ErrorResponse.builder()
			.status(customException.getHttpStatus().toString())
			.message(customException.getErrorMessage())
			.code(customException.getCode())
			.build();
	}

	public static ErrorResponse fromUnexpected(Exception e) {
		return ErrorResponse.builder()
			.status(HttpStatus.INTERNAL_SERVER_ERROR.toString())
			.message(e.getMessage() != null ? e.getMessage() : "Unexpected error")
			.code("INTERNAL_SERVER_ERROR")
			.build();
	}
}