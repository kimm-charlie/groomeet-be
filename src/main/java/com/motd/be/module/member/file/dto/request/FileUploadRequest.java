package com.motd.be.module.member.file.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class FileUploadRequest {

	@NotBlank(message = DIRECTORY_REQUIRED)
	private String directoryType;
	@NotBlank(message = FILE_TYPE_REQUIRED)
	private String fileType;
	@NotBlank(message = FILE_EXTENSION_REQUIRED)
	private String fileExtension;
	private String fileName;
	@NotBlank(message = FILE_SIZE_REQUIRED)
	private String fileSize;
}
