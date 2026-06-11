package com.motd.be.common.constants;

import java.time.ZoneId;
import java.util.List;

import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;

public class Constants {

	//log 관련 상수
	public static final String REQUEST_LOGGING_ID = "requestLoggingId";
	public static final String REQUEST_BODY = "requestBody";
	public static final String REQUEST_URL = "requestUrl";
	public static final String UNHANDLED_EXCEPTION = "UNHANDLED_EXCEPTION";
	public static final String HANDLED_EXCEPTION = "HANDLED_EXCEPTION";
	public static final String HEALTH_CHECK_PATH = "/health";
	public static final String HTTP_METHOD = "httpMethod";
	public static final String HOST = "host";
	public static final String X_FORWARDED_FOR = "X-Forwarded-For";
	public static final String X_FORWARDED_FOR_WITHOUT_UNDER_BAR = "xForwardedFor";
	public static final String X_REAL_IP_WITHOUT_UNDER_BAR = "xRealIP";
	public static final String X_REAL_IP = "X_Real_IP";
	public static final String RESPONSE_STATUS = "responseStatus";
	public static final String REQUEST = "REQUEST";
	public static final String PROMETHEUS_URL = "management";
	public static final String UTF_8 = "UTF-8";
	public static final String PROMETHEUS_URI = "/internal/metrics/prometheus";

	//Auth 관련 상수
	public static final String ID = "id";
	public static final String UUID_KEY = "uuid";
	public static final String ROLE = "role";
	public static final String IS_BANNED = "isBanned";
	public static final String UNBANNED_AT = "unbannedAt";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String IS_EXISTING_MEMBER = "isExistingMember";
	public static final String ACCESS_TOKEN = "accessToken";
	public static final String REFRESH_TOKEN = "refreshToken";
	public static final String BLANK = "";
	public static final String DEFAULT_NICKNAME = "GROO_";
	public static final String SIGN_UP = "signUp";
	public static final String REISSUE_PATH = "/api/members/reissue";
	public static final String SIGN_OUT_PATH = "/api/members/signOut";
	public static final String STRICT = "Strict";
	public static final String BRIDGE = "bridge";
	public static final String CLIENT_TYPE = "Client-Type";
	public static final String ALL_PATH = "/";

	// AppleOAuth  관련 상수
	public static final String EMAIL = "email";
	public static final String SUB = "sub";
	public static final String APPLE_REFRESH_TOKEN = "APPLE_REFRESH_TOKEN";
	public static final String CLIENT_SECRET = "client_secret";
	public static final String CODE = "code";
	public static final String BRIDGE_CODE = "bridgeCode";
	public static final String GRANT_TYPE = "grant_type";
	public static final String AUTHORIZATION_CODE = "authorization_code";
	public static final String CLIENT_ID = "client_id";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String TOKEN = "token";
	public static final String APPLE_REVOKE_URL = "/auth/revoke";
	public static final String APPLE_ISSUE_URL = "/auth/token";
	public static final String REFRESH_TOKEN_UNDER_BAR = "refresh_token";
	public static final String REDIRECT_URI = "redirect_uri";

	// jwt 관련 상수
	public static final String CREATED_MILLIS = "createdMillis";

	// exception 관련 상수
	public static final String ERROR_STATUS = "status";
	public static final String ERROR_MESSAGE = "message";
	public static final String CAUSE = "cause";
	public static final String TRACE = "trace";
	public static final String ERROR_CODE = "code";
	public static final String ERROR_TYPE = "type";

	// filter 관련 상수
	public static final String HEADER_AUTHORIZATION = "Authorization";
	public static final String TOKEN_PREFIX = "Bearer ";

	// version 관련 상수
	public static final String TYPE = "type";

	// paging 관련 상수
	public static final String PAGE = "page";

	// popup 관련 상수
	public static final String ZERO = "0";
	public static final String SHOW_IS_DELETED = "showIsDeleted";

	//portfolio 관련 상수
	public static final String PORTFOLIO_ID = "portfolioId";
	public static final String IS_POPULAR = "isPopular";
	public static final String CURSOR_ID = "cursorId";
	public static final String LOCATION_ID = "locationId";
	public static final String DIRECTOR_SERVICE_ID = "directorServiceId";
	public static final String SORT_TYPE = "sortType";
	public static final String EXCLUDE_PORTFOLIO_ID = "excludePortfolioId";
	public static final String TARGET_MEMBER_ID = "targetMemberId";

	//member 관련 상수
	public static final String PROFILE_IMAGE = "profileImage";
	public static final String ROLE_MEMBER = "ROLE_MEMBER";
	public static final String ROLE_DIRECTOR = "ROLE_DIRECTOR";
	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	public static final String MEMBER_ID = "memberId";
	public static final String NICKNAME = "nickname";
	public static final String IS_DIRECTOR = "isDirector";
	public static final String SEARCH = "search";
	public static final String SHOW_ONLY_DIRECTOR = "showOnlyDirector";
	public static final String SHOW_ONLY_MEMBER = "showOnlyMember";
	public static final String BAN_PERIOD_REQUIRED = "밴 기간은 필수입니다.";

	//location 관련 상수
	public static final String LOCATION_PARENT_ID = "locationParentId";
	public static final String DEFAULT_LOCATION_ALL_SUFFIX = " 전체";
	public static final String SPACE = " ";

	//serviceEstimateTemplate 관련 상수
	public static final String SERVICE_ID = "serviceId";
	public static final String TEMPLATE_ID = "templateId";

	// directorService 관련 상수
	public static final String DIRECTOR_SERVICE_PARENT_ID = "directorServiceParentId";
	public static final String PATH_SEPARATOR = " > ";
	public static final Integer ACTIVATION_REQUIRED_MINIMUM_DIRECTOR_COUNT = 20;
	public static final String PARENT_ID = "parentId";

	// serviceRequest 관련 상수
	public static final String STATUS = "status";
	public static final String PENDING = "pending";
	public static final String 협의후_결정 = "협의후 결정";
	public static final String SHOW_ONLY_PENDING = "showOnlyPending";
	public static final String SHOW_ALL = "showAll";
	public static final String SHOW_ONLY_DIRECT_REQUEST = "showOnlyDirectRequest";
	public static final List<ServiceRequestStatus> ENDED_SERVICE_REQUEST_STATUSES = List.of(
		ServiceRequestStatus.COMPLETED, ServiceRequestStatus.CANCELED, ServiceRequestStatus.EXPIRED);

	//serviceQuestion 관련 상수
	public static final String QUESTION = "question";

	//serviceEstimate 관련 상수
	public static final String SERVICE_REQUEST_ID = "serviceRequestId";
	public static final String SERVICE_ESTIMATE_ID = "serviceEstimateId";
	public static final String REVIEW_ID = "reviewId";
	public static final List<ServiceEstimateStatus> COMPLETED_ESTIMATE_STATUSES = List.of(
		ServiceEstimateStatus.COMPLETED_BY_MEMBER, ServiceEstimateStatus.REVIEW_COMPLETED);
	public static final List<ServiceEstimateStatus> ENDED_ESTIMATE_STATUSES = List.of(ServiceEstimateStatus.EXPIRED,
		ServiceEstimateStatus.CANCELED, ServiceEstimateStatus.COMPLETED_BY_MEMBER,
		ServiceEstimateStatus.REVIEW_COMPLETED);
	public static final List<ServiceEstimateStatus> ONGOING_ESTIMATE_STATUSES = List.of(ServiceEstimateStatus.ONGOING,
		ServiceEstimateStatus.DIRECTOR_DONE);

	//redis 관련 상수
	public static final ZoneId KST = ZoneId.of("Asia/Seoul");
	public static final String REDIS_ACTIVE_ACCESS_TOKEN_PREFIX = "auth:access-token:active:";
	public static final String BLACKLIST_FOR_SIGN_OUT_PREFIX = "auth:access-token:blacklist:sign_out:";
	public static final String BLACKLIST_FOR_BAN_PREFIX = "auth:access-token:blacklist:banned:";
	public static final String REDIS_CHAT_ROOM_SUBSCRIBE_KEY_PREFIX = "chat:room:subscribe:";
	public static final String REDIS_REISSUE_PREFIX = "auth:refresh-token:reissue:";
	public static final String REDIS_SERVICE_REQUEST_EXPIRE_KEY_PREFIX = "service:request:expire";
	public static final String REDIS_SERVICE_REQUEST_LOCATION_EXPAND_KEY_PREFIX = "service:request:location:expand";
	public static final String REDIS_SSE_KEY_PREFIX = "sse:connection:count:";
	public static final String REDIS_SSE_EVENT_BUFFER_KEY_PREFIX = "sse:event:buffer:";
	public static final String REDIS_DIRECTOR_DELETE_REQUEST_KEY_PREFIX = "director:hidden_requests:";
	public static final String MOBILE_OK_KEY_FOR_APP_REDIS_KEY_PREFIX = "mobile-ok:auth:";
	public static final int MOBILE_OK_KEY_FOR_APP_EXPIRY_MINUTES = 30;

	//chatRoom 관련 상수
	public static final String CHAT_ROOM_ID = "chatRoomId";
	public static final String CHAT_MESSAGE_ID = "chatMessageId";
	public static final String LAST_MESSAGE_ID = "lastMessageId";
	public static final String SHOW_ONLY_HIRED = "showOnlyHired";
	public static final String WORD = "word";
	public static final String SHOW_ONLY_UNREAD = "showOnlyUnread";

	//stomp 관련 상수
	public static final String RUNTIME_ERROR = "RUNTIME_ERROR";
	public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

	//memberMetadata 관련 상수
	public static final String DEVICE_TYPE = "deviceType";

	// sse 관련 상수
	public static final String LAST_EVENT_ID = "Last-Banner-ID";

	// story 관련 상수
	public static final String STORY_ID = "storyId";

	// cash 관련 상수
	public static final String CHAT_START = "type=chatStart";
	public static final int CASH_HISTORY_FIND_ALL_SIZE = 20;
	public static final String CASH_TRANSACTION_TYPE = "cashTransactionType";

	// notification 관련 상수
	public static final String NOTIFICATION_CATEGORY_TYPE = "notificationCategoryType";

	// file 관련 상수
	public static final String X_API_KEY = "x-api-key";

	// mobileOK 관련 상수
	public static final String CLIENT_PREFIX = "GROO_";
	public static final String DATA = "data";
	public static final String MOBILE_OK_KEY_FOR_APP = "mobileOkKeyForApp";

	// hackle 관련 상수
	public static final String X_HACKLE_API_KEY = "X-HACKLE-API-KEY";
	public static final String USER_ID = "userId";

	// push 관련 상수
	public static final String SENDER_NAME = "senderName";
	public static final String DIRECTOR_NAME = "directorName";
	public static final String CONTENT = "content";
	public static final String REFERENCE_TYPE = "referenceType";
	public static final String SERVICE_NAME = "serviceName";
	public static final String REFERENCE_ID = "referenceId";
	public static final String CLICK_ACTION = "click_action";
	public static final String FLUTTER_NOTIFICATION_CLICK = "FLUTTER_NOTIFICATION_CLICK";
	public static final String RECEIVER_TYPE = "receiverType";
	public static final String DEEP_LINK = "deepLink";
	public static final String RECEIVER_NAME = "receiverName";

	// fcmToken 관련 상수
	public static final String FCM_TOKEN_ID = "fcmTokenId";

	// s3 관련 상수
	public static final long IMAGE_RESIZE_THRESHOLD_BYTES = 1024 * 1024; // 1MB

	// dateFormat 관련 상수
	public static final String DATE_FORMAT = "yyyy.MM.dd";

	// timeSlot 관련 상수
	public static final String DATE_STR = "date";
	public static final String DIRECTOR_MEMBER_ID = "directorMemberId";
}
