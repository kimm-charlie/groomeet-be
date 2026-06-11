package com.motd.be.utils;

import java.util.List;

public class HackleUtils {

	public static <T> T findPublishedRequest(
		List<Object> publishedRequests,
		Class<T> type
	) {
		return publishedRequests.stream()
			.filter(type::isInstance)
			.map(type::cast)
			.findFirst()
			.orElseThrow(() ->
				new AssertionError("Published request not found: " + type.getSimpleName())
			);
	}
}
