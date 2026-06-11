package com.motd.be.shared.aws.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GeneratedPresignedUrl {

	private String originUrl;
	private String presignedUrl;
	private String fileKey;
	private boolean isPublicBucket;

	public static GeneratedPresignedUrl of(String presignedUrl, String originUrl, String fileKey,
		boolean isPublicBucket) {
		return GeneratedPresignedUrl.builder()
			.originUrl(originUrl)
			.presignedUrl(presignedUrl)
			.fileKey(fileKey)
			.isPublicBucket(isPublicBucket)
			.build();
	}

}
