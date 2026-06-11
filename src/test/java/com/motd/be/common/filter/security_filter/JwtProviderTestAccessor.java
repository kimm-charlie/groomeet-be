package com.motd.be.common.filter.security_filter;

import java.lang.reflect.Field;
import java.security.Key;

import com.motd.be.module.member.jwt.JwtProvider;

public class JwtProviderTestAccessor {

	public static void setKeys(Key accessKey, Key refreshKey) {
		try {
			Field accessField = JwtProvider.class.getDeclaredField("accessTokenKey");
			accessField.setAccessible(true);
			accessField.set(null, accessKey);

			Field refreshField = JwtProvider.class.getDeclaredField("refreshTokenKey");
			refreshField.setAccessible(true);
			refreshField.set(null, refreshKey);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
