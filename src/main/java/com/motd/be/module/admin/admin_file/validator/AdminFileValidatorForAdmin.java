package com.motd.be.module.admin.admin_file.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.FileException;

@Component
public class AdminFileValidatorForAdmin {

	@Value("${aws.lambda.secret-key}")
	private String lambdaSecretKey;

	public void validateApiKey(String apiKey) {
		if (!lambdaSecretKey.equals(apiKey)) {
			throw new CustomRuntimeException(FileException.UNAUTHORIZED_ACCESS);
		}
	}
}
