package com.motd.be.shared.aws.entity;

import java.io.IOException;
import java.util.UUID;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.AwsException;
import com.motd.be.shared.aws.enums.UploadFileType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UploadFile {

	private final String originalFilename;
	private final String contentType;
	private final byte[] fileBytes;
	private final long fileSize;

	public static UploadFile from(MultipartFile multipartFile, UploadFileType fileType) {
		validateFileExtension(multipartFile.getOriginalFilename(), fileType);
		try {
			return new UploadFile(multipartFile.getOriginalFilename(), multipartFile.getContentType(),
				multipartFile.getBytes(), multipartFile.getSize());
		} catch (IOException ex) {
			throw new CustomRuntimeException(AwsException.FAIL_TO_CONVERT_FILE);
		}
	}

	public static UploadFile from(String originalFilename, String contentType, byte[] fileBytes,
		UploadFileType fileType) {
		validateFileExtension(originalFilename, fileType);
		return new UploadFile(originalFilename, contentType, fileBytes, fileBytes.length);
	}

	private static void validateFileExtension(String originalFilename, UploadFileType fileType) {
		String extension = StringUtils.getFilenameExtension(originalFilename);
		fileType.validateExtension(extension);
	}

	public String getSavedFileName() {
		return originalFilename + UUID.randomUUID() + "." + StringUtils.getFilenameExtension(originalFilename);
	}
}
