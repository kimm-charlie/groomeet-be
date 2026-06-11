package com.motd.be.shared.aws.util;

public class S3Utils {

	public static long parseFileSizeOrDefault(String fileSize) {
		if (fileSize == null || fileSize.isBlank()) {
			return 0L;
		}
		try {
			return Long.parseLong(fileSize);
		} catch (NumberFormatException e) {
			return 0L;
		}
	}
}
