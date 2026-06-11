package com.motd.be.common.constants;

public class ValidationConstants {

	// serviceRequest
	public static final Integer MAX_RECEIVED_ESTIMATE_COUNT = 5;
	public static final int MAX_FILE_UPLOAD_COUNT_IN_SERVICE_REQUEST = 4;
	public static final int MAX_ADDITIONAL_REQUEST_LENGTH = 1000;
	public static final int MAX_LOCATION_COUNT_IN_SERVICE_REQUEST = 3;
	public static final int MAX_AI_CONTENT_LENGTH = 1000;
	public static final int MAX_WISH_TIME_COUNT = 3;
	public static final int MIN_WISH_TIME_COUNT = 1;

	// serviceEstimate
	public static final int SERVICE_ESTIMATE_MAX_TITLE_LENGTH = 30;
	public static final int SERVICE_ESTIMATE_MAX_CONTENT_LENGTH = 1000;
	public static final long SERVICE_ESTIMATE_MAX_PRICE = 10_000_000_000L;
	public static final int SERVICE_ESTIMATE_FILE_MAX_COUNT = 5;

	// portfolio
	public static final int PORTFOLIO_MAX_TITLE_LENGTH = 20;
	public static final int PORTFOLIO_MAX_CONTENT_LENGTH = 2000;
	public static final long PORTFOLIO_MAX_PRICE = 10_000_000_000L;
	public static final int PORTFOLIO_MAX_IMAGE_COUNT = 10;

	// chatMessage
	public static final int CHAT_MESSAGE_MAX_LENGTH = 500;
	public static final int CHAT_MESSAGE_IMAGE_MAX_COUNT = 9;

	// review
	public static final int REVIEW_MAX_TITLE_LENGTH = 30;
	public static final int REVIEW_MAX_CONTENT_LENGTH = 300;
	public static final int REVIEW_MAX_IMAGE_COUNT = 5;

	// report
	public static final int REPORT_MAX_DESCRIPTION_LENGTH = 1000;
	public static final int REPORT_MAX_IMAGE_COUNT = 5;

	// serviceEstimatetemplate
	public static final int SERVICE_ESTIMATE_TEMPLATE_MAX_LIMIT_COUNT = 3;

	// businessRegistration
	public static final int BUSINESS_REGISTRATION_NUMBER_LENGTH = 50;
	public static final int BUSINESS_REGISTRATION_RESIDENT_NUMBER_LENGTH = 50;
	public static final int BUSINESS_REGISTRATION_FILE_MAX_COUNT = 10;

	// member
	public static final String REFERRAL_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	public static final int REFERRAL_CODE_LENGTH = 6;

	// promotionCode
	public static final int DIRECTOR_ONBOARDING_EXTENSION = 14;

	// prompt
	public static final int MIN_LOCATION_COUNT_IN_PROMPT = 1;
	public static final int MAX_LOCATION_COUNT_IN_PROMPT = 3;
	public static final int MAX_FILE_COUNT_IN_PROMPT = 3;
	public static final int MAX_AI_PROMPT_TEXT_LENGTH = 1000;
	public static final int MAX_PROMPT_ROOM_TURN_COUNT = 10;


	// consultingSheet
	public static final int CONSULTING_SHEET_MAX_CONTENT_LENGTH = 2000;
	public static final int CONSULTING_SHEET_MAX_PRICE_LENGTH = 50;
	public static final int CONSULTING_SHEET_FILE_MAX_COUNT = 5;

	// consultingRequest
	public static final int CONSULTING_REQUEST_MAX_FILES_PER_CATEGORY = 3;

	//auth
	public static final int WITHDRAWAL_RESTRICTION_MONTHS = 1;

}
