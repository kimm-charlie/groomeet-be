package com.motd.be.module.member.file.dto.response;

import com.motd.be.module.member.file.entity.BaseFile;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadResponse {

	private String presignedUrl;
	private Long imageId;
	private String cdnUrl;

	public static FileUploadResponse of(String presignedUrl, BaseFile image) {
		return FileUploadResponse.builder()
			.presignedUrl(presignedUrl)
			.imageId(image.getId())
			.cdnUrl(image.getCdnUrl())
			.build();
	}

}
