package com.motd.be.common.constants;

import java.time.Duration;
import java.time.LocalDate;

public class TimePolicy {

	// review
	public static final int REVIEW_WRITE_EXPIRE_DAYS = 7;
	public static final int REVIEW_REMINDER_AFTER_DAYS = 1;
	public static final int REVIEW_REMINDER_TIME_TOLERANCE_HOURS = 1;

	// director
	public static final int DIRECTOR_ONBOARDING_PASS_FREE_MONTH = 1;
	public static final LocalDate ONBOARDING_ANCHOR_DATE = LocalDate.of(2026, 2, 26);

	// serviceEstimate
	public static final int DIRECTOR_COMPLETE_AUTO_CONFIRM_DAYS = 1;
	public static final int MEMBER_COMPLETE_AUTO_CONFIRM_DAYS = 3;

	// serviceRequest
	public static final Integer SERVICE_REQUEST_EXPIRE_DAYS = 1;
	public static final Integer SERVICE_REQUEST_EXPIRE_HOURS = 24;
	public static final int SERVICE_REQUEST_LOCATION_EXPAND_HOURS = 12;

	// jwt
	public static final long ACCESS_TOKEN_EXPIRE_SECOND = 60 * 60 * 24;
	public static final long REFRESH_TOKEN_EXPIRE_SECOND = 60 * 60 * 24 * 7;
	public static final long COOKIE_JWT_TOKEN_EXPIRE_SECOND = 60 * 60 * 24 * 7;
	public static final long COOKIE_JWT_TOKEN_EXPIRE_SECOND_FOR_ADMIN = 60 * 60 * 24;

	// aws
	public static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofMinutes(15); // 15분 만료

	// redis
	public static final Duration REDIS_ACTIVE_ACCESS_TOKEN_TTL = Duration.ofDays(1);
	public static final int REDIS_ACCESS_TOKEN_BLACKLIST_TTL_MINUTE = 60 * 24;
	public static final Duration REDIS_REISSUE_TTL = Duration.ofSeconds(30);
	public static final Duration REDIS_SSE_EVENT_BUFFER_KEY_TTL = Duration.ofSeconds(30);

	// sse
	public static final long SSE_EMITTER_DEFAULT_TIMEOUT_MILLIS = 60 * 3 * 1000L; // 3분 유지

	// consulting request
	public static final int CONSULTING_REQUEST_RESERVATION_MINUTES = 30;

	// mobile ok
	public static final int MOBILE_OK_COOKIE_MAX_AGE_SECONDS = 30 * 60; // 30분
}
