package com.motd.be.common.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DefaultConstants {

	public static String DEFAULT_PROFILE_IMAGE_URL;

	public DefaultConstants(@Value("${app.default-profile-image-url}") String defaultProfileImageUrl) {
		DEFAULT_PROFILE_IMAGE_URL = defaultProfileImageUrl;
	}
}
