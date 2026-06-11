package com.motd.be.module.member.file.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.FileException;
import com.motd.be.exception.exceptions.ImageException;
import com.motd.be.module.member.file.entity.BaseFile;

@Component
public class FileValidator {

	@Value("${aws.lambda.secret-key}")
	private String lambdaSecretKey;

	public void validateApiKey(String apiKey) {
		if (!lambdaSecretKey.equals(apiKey)) {
			throw new CustomRuntimeException(FileException.UNAUTHORIZED_ACCESS);
		}
	}

	public void validateOwnership(BaseFile baseFile, Long memberId) {
		if (!baseFile.isOwnedBy(memberId)) {
			throw new CustomRuntimeException(ImageException.NOT_OWNED_BY);
		}
	}
}
