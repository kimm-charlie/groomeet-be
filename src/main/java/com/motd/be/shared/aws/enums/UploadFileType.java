package com.motd.be.shared.aws.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.AwsException;

import lombok.Getter;

@Getter
public enum UploadFileType {

	IMAGE(Set.of("png", "jpg", "jpeg", "webp", "gif", "bmp")),
	DOCUMENT(Set.of("pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "hwp", "txt", "zip", "rar", "7z"));

	private final Set<String> allowedExtensions;
	private final long maxFileSizeInBytes = 30 * 1024 * 1024; // 30MB

	UploadFileType(Set<String> allowedExtensions) {
		this.allowedExtensions = allowedExtensions;
	}

	public static UploadFileType from(String type) {
		if (type == null || type.isBlank()) {
			return IMAGE;
		}

		return Arrays.stream(values())
			.filter(value -> value.name().equalsIgnoreCase(type))
			.findFirst()
			.orElseThrow(() -> new CustomRuntimeException(AwsException.INVALID_FILE_TYPE));
	}

	public void validateExtension(String extension) {
		if (extension == null) {
			throw new CustomRuntimeException(AwsException.INVALID_FILE_EXTENSION);
		}

		String lowerCaseExtension = extension.toLowerCase(Locale.ROOT);
		if (!allowedExtensions.contains(lowerCaseExtension)) {
			throw new CustomRuntimeException(AwsException.INVALID_FILE_EXTENSION);
		}
	}

	public void validateFileSize(String fileSize) {
		if (Long.parseLong(fileSize) > maxFileSizeInBytes) {
			throw new CustomRuntimeException(AwsException.FILE_SIZE_EXCEEDED);
		}
	}
}
