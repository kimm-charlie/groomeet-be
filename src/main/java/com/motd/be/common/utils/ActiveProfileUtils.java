package com.motd.be.common.utils;

import java.util.Arrays;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ActiveProfileUtils {

	private final Environment environment;

	public boolean isDevProfileActive() {
		return Arrays.stream(environment.getActiveProfiles())
			.anyMatch(p -> p.startsWith("dev") || p.startsWith("local")); // dev, dev-green, dev-blue 등 포함
	}
}
