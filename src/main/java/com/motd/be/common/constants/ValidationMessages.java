package com.motd.be.common.constants;

public class ValidationMessages {

	//appleOauth
	public static final String IDENTITY_TOKEN_REQUIRED = "애플 로그인시 identityToken은 필수입니다.";
	public static final String AUTHORIZATION_CODE_REQUIRED = "애플 로그인시 authorizationCode는 필수입니다.";

	//auth
	public static final String UUID_REQUIRED = "uuid는 필수입니다.";
	public static final String SERVICE_AGREEMENT_REQUIRED = "서비스 약관 동의는 필수입니다.";
	public static final String PRIVACY_POLICY_AGREEMENT_REQUIRED = "개인정보 처리방침 동의는 필수입니다.";
	public static final String MARKETING_AGREEMENT_REQUIRED = "마케팅 수신 동의여부는 필수입니다.";
	public static final String WITHDRAWAL_REASON_REQUIRED = "탈퇴 사유는 필수입니다.";

	//googleOauth
	public static final String ID_TOKEN_REQUIRED = "구글 로그인시 idToken은 필수입니다.";

	//kakaoOauth
	public static final String KAKAO_ACCESS_TOKEN_REQUIRED = "카카오 로그인시 accessToken은 필수입니다.";
	public static final String KAKAO_AUTHORIZATION_CODE_REQUIRED = "카카오 로그인시 authorizationCode는 필수입니다.";

	//file
	public static final String DIRECTORY_REQUIRED = "디렉토리는 필수입니다.";
	public static final String FILE_ID_REQUIRED = "파일 아이디는 필수적으로 필요합니다.";
	public static final String FILE_TYPE_REQUIRED = "파일 타입은 필수입니다.";
	public static final String FILE_EXTENSION_REQUIRED = "파일 확장자는 필수입니다.";
	public static final String FILE_SIZE_REQUIRED = "파일 확장자는 필수입니다.";

	// banner
	public static final String BANNER_TITLE_MAX_LENGTH_MSG = "배너 제목은 100자를 초과할 수 없습니다.";
	public static final String BANNER_START_AT_REQUIRED = "시작 시간은 필수입니다.";
	public static final String BANNER_END_AT_REQUIRED = "종료 시간은 필수입니다.";
	public static final String BANNER_SORT_ORDER_REQUIRED = "정렬 순서는 필수 입니다.";
	public static final String BANNER_IS_WEB_VIEW_REQUIRED = "웹뷰 배너 여부는 필수입니다.";
	public static final String BANNER_TYPE_REQUIRED = "배너 타입은 필수입니다.";
	public static final String BANNER_THUMBNAIL_FILE_ID_REQUIRED = "썸네일 파일 ID는 필수입니다.";

	// popup
	public static final String POPUP_TITLE_MAX_LENGTH_MSG = "팝업 제목은 100자를 초과할 수 없습니다.";
	public static final String POPUP_START_AT_REQUIRED = "시작 시간은 필수입니다.";
	public static final String POPUP_END_AT_REQUIRED = "종료 시간은 필수입니다.";
	public static final String POPUP_SORT_ORDER_REQUIRED = "정렬 순서는 필수 입니다.";
	public static final String POPUP_THUMBNAIL_FILE_ID_REQUIRED = "팝업 썸네일 파일 ID는 필수입니다.";
	public static final String POPUP_TYPE_REQUIRED = "팝업 타입은 필수입니다.";
	//member
	public static final String NICKNAME_REQUIRED = "닉네임은 공백일 수 없습니다.";
	public static final String NICKNAME_MAX_LENGTH = "닉네임은 최대 12자 이내로 입력해야 합니다.";
	public static final String TO_DEFAULT_IMAGE_NEEDED = "기본 이미지 변경 여부는 필수입니다.";

	//memberMetadata
	public static final String DEVICE_TYPE_REQUIRED = "기기타입은 필수값 입니다.";
	public static final String VERSION_REQUIRED = "버전은 필수값 입니다.";

	//portfolio
	public static final String TITLE_REQUIRED = "포트폴리오 제목은 필수입니다.";
	public static final String DIRECTOR_SERVICE_ID_REQUIRED = "디렉터 서비스 선택은 필수입니다.";
	public static final String IMAGE_IDS_REQUIRED = "포트폴리오 이미지는 최소 1개 이상 등록해야 합니다.";
	public static final String THUMBNAIL_IMAGE_ID_REQUIRED = "포트폴리오 대표 이미지는 필수입니다.";
	public static final String PRICE_REQUIRED = "가격은 필수입니다.";
	public static final String CONTENT_REQUIRED = "상세 설명을 입력해야 합니다.";
	public static final String PORTFOLIO_TITLE_MAX_LENGTH_MSG = "포트폴리오 제목은 최대 20자까지 입력할 수 있습니다.";
	public static final String PORTFOLIO_IMAGE_MAX_COUNT_MSG = "포트폴리오 이미지는 최대 10개까지 업로드 가능합니다.";
	public static final String PORTFOLIO_MAX_PRICE_MSG = "가격은 최대 100억원 이내여야 합니다.";
	public static final String PORTFOLIO_MAX_CONTENT_LENGTH_MSG = "상세 설명은 최대 2000자까지 입력할 수 있습니다.";

	// directorService
	public static final String LOCATION_MUST_BE_SELECTED = "지역은 최소 1개 이상 선택해야 합니다.";
	public static final String DIRECTOR_SERVICE_MUST_BE_SELECTED = "서비스는 최소 1개 이상 선택해야 합니다.";
	public static final String DIRECT_REQUEST_MEMBER_ID_REQUIRED = "다이렉트 요청을 할 디렉터 ID 가 필요합니다.";
	public static final String DIRECTOR_SERVICE_SELECTION_OUT_OF_BOUNDS = "서비스의 갯수는 최소 1개 이상 7개 이하 여야 합니다.";
	public static final String GENDER_MUST_BE_SELECTED = "성별을 필수적으로 선택해야 합니다.";
	public static final String DIRECTOR_SERVICE_NAME_REQUIRED = "서비스 이름은 필수입니다.";
	public static final String DIRECTOR_SERVICE_IS_ACTIVE_REQUIRED = "활성화 여부는 필수입니다.";
	public static final String DIRECTOR_SERVICE_SORT_ORDER_REQUIRED = "정렬 순서는 필수입니다.";

	//serviceEstimate
	public static final String SERVICE_ID_REQUIRED_MSG = "서비스 요청 ID는 필수입니다.";
	public static final String SERVICE_REQUEST_ID_REQUIRED_MSG = "서비스 요청 ID는 필수입니다.";
	public static final String ESTIMATE_TITLE_REQUIRED_MSG = "제안 제목은 필수입니다.";
	public static final String SERVICE_ESTIMATE_TITLE_LENGTH_MSG = "제안 제목은 최대 30글자 입니다.";
	public static final String ESTIMATE_PRICE_REQUIRED_MSG = "제안 가격은 필수입니다.";
	public static final String ESTIMATE_CONTENT_REQUIRED_MSG = "제안 내용은 필수입니다.";
	public static final String SERVICE_ESTIMATE_MAX_PRICE_MSG = "가격은 최대 100억원 이내여야 합니다.";
	public static final String SERVICE_ESTIMATE_CONTENT_MAX_LENGTH_MSG = "내용은 최대 1000글자 이내여야 합니다.";
	public static final String SERVICE_ESTIMATE_FILE_MAX_COUNT_MSG = "제안서 첨부 파일은 최대 5개 까지 업로드 가능합니다.";
	public static final String ESTIMATE_SCHEDULED_AT_REQUIRED_MSG = "제안 시간 선택은 필수입니다.";
	public static final String ESTIMATE_SCHEDULED_AT_INVALID_FORMAT_MSG = "제안 시간은 yyyy.MM.dd HH:mm 형식이어야 합니다.";

	// chatMessage
	public static final String CHAT_ROOM_ID_REQUIRED = "채팅방 ID는 필수입니다.";
	public static final String IMAGE_REQUIRED = "채팅 이미지를 보낼때, 이미지는 필수적으로 필요합니다.";
	public static final String CHAT_MESSAGE_IMAGE_MAX_COUNT_MSG = "채팅 이미지는 최대 9개까지 전송할 수 있습니다.";

	// review
	public static final String REVIEW_TITLE_REQUIRED = "리뷰 제목은 필수입니다.";
	public static final String REVIEW_CONTENT_REQUIRED = "리뷰 내용은 필수입니다.";
	public static final String REVIEW_TITLE_MAX_LENGTH_MSG = "리뷰 제목은 최대 30자까지 입력할 수 있습니다.";
	public static final String REVIEW_CONTENT_MAX_LENGTH_MSG = "리뷰 내용은 최대 300자까지 입력할 수 있습니다.";
	public static final String REVIEW_FILE_MAX_COUNT_MSG = "리뷰 이미지는 최대 5개까지 업로드 가능합니다.";

	// memberBlock
	public static final String BLOCKED_ID_REQUIRED = "차단할 회원 ID는 필수입니다.";

	// memberDirectorFavorite
	public static final String TARGET_ID_REQUIRED = "즐겨찾기할 회원 ID는 필수입니다.";

	// memberReport
	public static final String REPORTED_ID_REQUIRED = "신고할 대상의 ID는 필수입니다.";
	public static final String REPORTED_REASON_REQUIRED = "신고 이유는 필수입니다.";
	public static final String REPORTED_TYPE_REQUIRED = "신고 타입은 필수입니다.";
	public static final String REPORT_DESCRIPTION_MAX_LENGTH_MSG = "상세 설명은 최대 1000자까지 입력할 수 있습니다.";
	public static final String REPORT_IMAGE_MAX_COUNT_MSG = "신고 이미지는 최대 5개까지 업로드 가능합니다.";

	// notification
	public static final String PUSH_TYPE_NOT_BLANK = "푸시 알림 값은 필수 입니다.";

	// businessRegistration
	public static final String BUSINESS_REGISTRATION_NUMBER_REQUIRED = "사업자등록번호는 필수입니다.";
	public static final String BUSINESS_REGISTRATION_NUMBER_MAX_LENGTH_MSG = "사업자등록번호는 최대 50자까지 입력할 수 있습니다.";
	public static final String RESIDENT_REGISTRATION_NUMBER_REQUIRED = "주민등록번호는 필수입니다.";
	public static final String RESIDENT_REGISTRATION_NUMBER_MAX_LENGTH_MSG = "주민등록번호는 최대 50자까지 입력할 수 있습니다.";
	public static final String BUSINESS_REGISTRATION_FILE_REQUIRED = "사업자 등록증 파일은 최소 1개 이상 등록해야 합니다.";
	public static final String BUSINESS_REGISTRATION_FILE_MAX_COUNT_MSG = "사업자 등록증 파일은 최대 10개까지 업로드 가능합니다.";

	// cash
	public static final String AMOUNT_REQUIRED = "금액은 필수입니다.";
	public static final String REFERENCE_ID_REQUIRED = "참조 아이디는 필수입니다.";

	// directorLocationMapping
	public static final String INTRODUCE_TEXT_OUT_OF_BOUND = "소개 글은 최대 100자까지 입력할 수 있습니다.";

	//serviceRequest
	public static final String FILE_UPLOAD_EXCEED_MAX_COUNT = "요청에 첨부 가능한 파일은 최대 4개 까지입니다.";
	public static final String ADDITIONAL_REQUEST_EXCEED_MAX_LENGTH = "추가 요청은 최대 1000자 까지 입력할 수 있습니다.";
	public static final String LOCATION_EXCEED_MAX_COUNT = "지역은 최대 3개까지 선택이 가능 합니다.";
	public static final String AI_CONTENT_REQUIRED = "AI 요청 내용은 필수입니다.";
	public static final String AI_CONTENT_EXCEED_MAX_LENGTH = "AI 요청 내용은 최대 1000자 까지 입력할 수 있습니다.";
	public static final String WISH_TIME_REQUIRED = "희망 시간은 최소 1개 이상 선택해야 합니다.";
	public static final String WISH_TIME_EXCEED_MAX_COUNT = "희망 시간은 최대 3개까지 선택이 가능합니다.";
	public static final String WISH_TIME_INVALID_FORMAT = "희망 시간은 yyyy.MM.dd HH:mm 형식이어야 합니다.";

	// prompt
	public static final String PROMPT_DIRECTOR_SERVICE_ID_REQUIRED = "서비스 선택은 필수입니다.";
	public static final String PROMPT_LOCATION_SIZE = "희망 지역은 최소 1개, 최대 3개까지 선택할 수 있습니다.";
	public static final String PROMPT_FILE_IDS_SIZE = "첨부 이미지 파일 ID는 최대 3개까지 입력할 수 있습니다.";

	// promotionCode
	public static final String PROMOTION_CODE_REQUIRED = "프로모션 코드는 공백일 수 없습니다.";

	// consultingSheet
	public static final String CONSULTING_SHEET_CONTENT_REQUIRED = "컨설팅지 내용은 필수입니다.";
	public static final String CONSULTING_SHEET_PRICE_REQUIRED = "컨설팅지 가격은 필수입니다.";
	public static final String CONSULTING_SHEET_CONTENT_MAX_LENGTH_MSG = "컨설팅지 내용은 최대 2000자까지 입력할 수 있습니다.";
	public static final String CONSULTING_SHEET_PRICE_MAX_LENGTH_MSG = "컨설팅지 가격은 최대 50자까지 입력할 수 있습니다.";
	public static final String CONSULTING_SHEET_FILE_MAX_COUNT_MSG = "컨설팅지 첨부 파일은 최대 5개까지 업로드 가능합니다.";
	public static final String CONSULTING_SHEET_REQUEST_ID_REQUIRED = "컨설팅 요청 ID는 필수입니다.";

	// consultingRequest
	public static final String CONSULTING_USES_HAIR_PRODUCT_REQUIRED = "헤어 제품 사용 여부는 필수입니다.";
	public static final String CONSULTING_PREFERS_EXPOSED_FOREHEAD_REQUIRED = "이마 노출 선호 여부는 필수입니다.";
	public static final String CONSULTING_FILES_REQUIRED = "컨설팅 이미지 파일은 최소 1개 이상이어야 합니다.";
	public static final String CONSULTING_IMAGE_CATEGORY_REQUIRED = "이미지 카테고리는 필수입니다.";
	public static final String CONSULTING_RECENT_PROCEDURE_REQUIRED = "최근 시술 여부는 필수입니다.";
	public static final String CONSULTING_RECENT_PROCEDURE_MAX_LENGTH = "최근 시술 여부는 최대 50자까지 입력할 수 있습니다.";
	public static final String CONSULTING_LOCATIONS_REQUIRED = "지역은 최소 1개 이상 선택해야 합니다.";
	public static final String CONSULTING_LOCATIONS_SIZE = "지역은 최대 3개까지 선택이 가능합니다.";

}
