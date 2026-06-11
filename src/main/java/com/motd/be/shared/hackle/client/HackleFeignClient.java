package com.motd.be.shared.hackle.client;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.motd.be.common.config.HackleFeignConfig;
import com.motd.be.shared.hackle.dto.request.HackleRequest;
import com.motd.be.shared.hackle.dto.request.HackleUpdateKakaoSubscriptionRequest;
import com.motd.be.shared.hackle.dto.request.HackleUpdatePhoneNumberRequest;
import com.motd.be.shared.hackle.dto.request.HackleUpdatePushSubscriptionRequest;
import com.motd.be.shared.hackle.dto.request.HackleUserPropertyRequest;
import com.motd.be.shared.hackle.dto.response.HackleSuccessResponse;

@FeignClient(
	name = "hackleClient",
	url = "${hackle.api.base-url}",
	configuration = HackleFeignConfig.class
)
public interface HackleFeignClient {

	@PostMapping(
		value = "/v1/push-message/trigger/send",
		consumes = MediaType.APPLICATION_JSON_VALUE
	)
	HackleSuccessResponse sendPushMessage(
		@RequestHeader(X_HACKLE_API_KEY) String apiKey,
		@RequestBody HackleRequest request
	);

	@PostMapping(
		value = "/v1/kakao-message/trigger/send",
		consumes = MediaType.APPLICATION_JSON_VALUE
	)
	HackleSuccessResponse sendKakaoMessage(
		@RequestHeader(X_HACKLE_API_KEY) String apiKey,
		@RequestBody HackleRequest request
	);

	@PostMapping(
		value = "/v1/properties",
		consumes = MediaType.APPLICATION_JSON_VALUE
	)
	Void updateUserProperties(
		@RequestHeader(X_HACKLE_API_KEY) String sdkKey,
		@RequestBody HackleUserPropertyRequest request
	);

	@PostMapping(
		value = "/v1/properties/phone-numbers",
		consumes = MediaType.APPLICATION_JSON_VALUE
	)
	Void updateUserPhoneNumbers(
		@RequestHeader(X_HACKLE_API_KEY) String apiKey,
		@RequestBody HackleUpdatePhoneNumberRequest request
	);

	@DeleteMapping(
		value = "/v1/properties/phone-numbers"
	)
	Void deleteUserPhoneNumbers(
		@RequestHeader(X_HACKLE_API_KEY) String apiKey,
		@RequestParam(USER_ID) String userId
	);

	@PostMapping(
		value = "/v1/push-subscriptions",
		consumes = MediaType.APPLICATION_JSON_VALUE
	)
	Void updatePushSubscription(
		@RequestHeader(X_HACKLE_API_KEY) String apiKey,
		@RequestBody HackleUpdatePushSubscriptionRequest request
	);

	@PostMapping(
		value = "/v1/kakao-subscriptions",
		consumes = MediaType.APPLICATION_JSON_VALUE
	)
	Void updateKakaoSubscription(
		@RequestHeader(X_HACKLE_API_KEY) String apiKey,
		@RequestBody HackleUpdateKakaoSubscriptionRequest request
	);
}
