package com.motd.be.exception;

/**
 * MobileOk 인증과정에서 발생할수 있는 오류를 위한 예외 클래스 이다.
 */
public class MobileOkCustomException extends RuntimeException {

	public MobileOkCustomException(String message) {
		super(message);
	}

	public MobileOkCustomException(String message, Throwable cause) {
		super(message, cause);
	}
}
