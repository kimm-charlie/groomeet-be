package com.motd.be.shared.aws.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageUrlConverter {

	private static String imageOriginS3Domain;
	private static String publicS3Domain;
	private static String cdnDomain;

	public ImageUrlConverter(
		@Value("${app.domain.image-origin-s3}") String imageOriginS3,
		@Value("${app.domain.public-s3}") String publicS3,
		@Value("${app.domain.cdn}") String cdn
	) {
		imageOriginS3Domain = imageOriginS3;
		publicS3Domain = publicS3;
		cdnDomain = cdn;
	}

	public static String toCdnUrl(String originalUrl) {
		if (originalUrl == null) {
			return null;
		}
		if (originalUrl.startsWith(imageOriginS3Domain)) {
			String cdnUrl = originalUrl.replace(imageOriginS3Domain, cdnDomain);

			return cdnUrl.replaceAll("\\.[^.]+$", ".webp");
		} else {
			return originalUrl.replace(publicS3Domain, cdnDomain);
		}
	}
}
