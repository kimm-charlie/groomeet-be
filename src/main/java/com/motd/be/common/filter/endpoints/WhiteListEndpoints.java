package com.motd.be.common.filter.endpoints;

import org.springframework.http.HttpMethod;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WhiteListEndpoints {

	// health, docs
	HEALTH("/health", HttpMethod.GET),

	DOCS("^/api/docs/.*$", HttpMethod.GET),

	MOBILE_OK_AUTHENTICATION("^/api/mobile-ok/authentication$", HttpMethod.POST),

	//admin
	ADMIN_SIGN_IN("^/api/admin/signIn.*$", HttpMethod.POST),
	ADMIN_FILE_PROCESS_CALLBACK("^/api/admin/files/processed$", HttpMethod.POST),

	//auth
	OAUTH_SIGN_IN("^/api/members/signIn.*$", HttpMethod.POST), SIGN_UP("/api/members/signUp.*",
		HttpMethod.POST), REISSUE("/api/members/reissue.*", HttpMethod.POST), EXCHANGE_CODE_FOR_TOKEN(
		"/api/members/bridge/token.*", HttpMethod.POST),

	//location
	LOCATION("^/api/locations.*$", HttpMethod.GET),

	//member
	MEMBER_CHECK_NICKNAME_DUPLICATE("^/api/members/nickname/duplicate-check$", HttpMethod.POST), MEMBER_PROFILE(
		"^/api/members/[0-9]*/profile$", HttpMethod.GET),

	// consulting
	CONSULTING_ELIGIBILITY("^/api/members/consulting/eligibility$", HttpMethod.GET),

	//popup
	POPUP("^/api/popups.*$", HttpMethod.GET),

	//portfolio
	PORTFOLIO("^/api/portfolios.*$", HttpMethod.GET),

	//director service
	DIRECTOR_SERVICE("^/api/director-services.*$", HttpMethod.GET), FIND_ALL_SPECIFIC_DIRECTOR_SERVICES(
		"^/api/directors/[0-9]*/services$", HttpMethod.GET),

	//director profile detail
	DIRECTOR_PROFILE_DETAIL_FIND_DETAIL_FOR_PUBLIC("^/api/directors/[0-9]*/profile-detail$", HttpMethod.GET),

	//sse
	SSE_CONNECT("^/api/sse/connect$", HttpMethod.GET),

	//story
	STORY_WITH_GET_METHOD("^/api/stories.*$", HttpMethod.GET),

	// rank view
	DIRECTOR_RANK("^/api/directors/rank.*$", HttpMethod.GET),

	// review
	REVIEW_FIND_BY_DIRECTOR("^/api/directors/[0-9]*/reviews.*$", HttpMethod.GET),

	// popular service set
	POPULAR_SERVICE_SET("^/api/popular-service-sets.*$", HttpMethod.GET),

	// event
	EVENT("^/api/banners.*$", HttpMethod.GET),

	// time-slot
	TIME_SLOT("^/api/time-slots.*$", HttpMethod.GET),

	// file
	FILE_PROCESS_CALLBACK("^/api/files/processed$", HttpMethod.POST),

	// fcmToken
	FCM_TOKEN("^/api/fcm-tokens$", HttpMethod.POST),

	// actuator - prometheus
	PROMETHEUS("^/internal/metrics/prometheus", HttpMethod.GET);

	private final String pattern;
	private final HttpMethod method;
}
