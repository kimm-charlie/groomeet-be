package com.motd.be.exception;

import org.springframework.http.HttpStatus;

public interface CustomException {
	HttpStatus getHttpStatus();

	String getErrorMessage();

	String getName();

	String getCode();
}
