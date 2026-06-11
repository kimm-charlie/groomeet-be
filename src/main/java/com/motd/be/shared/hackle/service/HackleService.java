package com.motd.be.shared.hackle.service;

import static com.motd.be.common.constants.BatchConstant.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.shared.hackle.client.HackleFeignClient;
import com.motd.be.shared.hackle.dto.request.HackleDeletePhoneNumberRequest;
import com.motd.be.shared.hackle.dto.request.HackleKakaoRequest;
import com.motd.be.shared.hackle.dto.request.HackleRequest;
import com.motd.be.shared.hackle.dto.request.HackleUpdateKakaoSubscriptionRequest;
import com.motd.be.shared.hackle.dto.request.HackleUpdatePhoneNumberRequest;
import com.motd.be.shared.hackle.dto.request.HackleUpdatePushSubscriptionRequest;
import com.motd.be.shared.hackle.dto.request.HackleUserPropertyRequest;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HackleService {

	@Value("${hackle.api.key}")
	private String apiKey;
	@Value("${hackle.sdk.key}")
	private String sdkKey;
	private final HackleFeignClient hackleFeignClient;

	public void updateKakaoSubscription(HackleUpdateKakaoSubscriptionRequest request) {
		try {
			hackleFeignClient.updateKakaoSubscription(apiKey, request);
		} catch (CustomRuntimeException e) {
			log.error("Hackle 카카오 구독 업데이트 실패 - memberId: {}, marketing: {}, information: {}", request.getUserId(),
				request.getMarketingSubscriptionStatus(), request.getInformationSubscriptionStatus(), e);
		} catch (FeignException e) {
			log.error("Hackle 카카오 구독 업데이트 Feign 에러 - memberId: {}, marketing: {}, information: {}", request.getUserId(),
				request.getMarketingSubscriptionStatus(), request.getInformationSubscriptionStatus(), e);
		}
	}

	public void updatePushSubscription(HackleUpdatePushSubscriptionRequest request) {
		try {
			hackleFeignClient.updatePushSubscription(apiKey, request);
		} catch (CustomRuntimeException e) {
			log.error("Hackle 푸시 구독 업데이트 실패 - memberId: {}, marketingAgreed: {}, informationAgreed: {}",
				request.getUserId(), request.getInformationSubscriptionStatus(),
				request.getInformationSubscriptionStatus());
		} catch (FeignException e) {
			log.error("Hackle 푸시 구독 업데이트 Feign 에러 - memberId: {}, marketingAgreed: {}, informationAgreed: {}",
				request.getUserId(), request.getInformationSubscriptionStatus(),
				request.getInformationSubscriptionStatus());
		}
	}

	public void updatePhoneNumber(HackleUpdatePhoneNumberRequest request) {
		try {
			hackleFeignClient.updateUserPhoneNumbers(apiKey, request);
		} catch (CustomRuntimeException e) {
			log.error("Hackle 전화번호 업데이트 실패 - memberId: {}, phoneNumber: {}", request.getUserId(),
				request.getPhoneNumber(), e);
		} catch (FeignException e) {
			log.error("Hackle 전화번호 업데이트 Feign 에러 - memberId: {}, phoneNumber: {}", request.getUserId(),
				request.getPhoneNumber(), e);
		}
	}

	public void deletePhoneNumber(HackleDeletePhoneNumberRequest request) {
		try {
			hackleFeignClient.deleteUserPhoneNumbers(apiKey, String.valueOf(request.getUserId()));
		} catch (CustomRuntimeException e) {
			log.error("Hackle 전화번호 삭제 실패 - memberId: {}", request.getUserId(), e);
		} catch (FeignException e) {
			log.error("Hackle 전화번호 업데이트 Feign 에러 - memberId: {}", request.getUserId(), e);
		}
	}

	public void updateUserIdentity(HackleUserPropertyRequest request) {
		try {
			if (request.getUserId() != null) {
				hackleFeignClient.updateUserProperties(sdkKey, request);
			}
		} catch (CustomRuntimeException e) {
			log.error("Hackle 사용자 식별정보 업데이트 실패 - memberId: {}, deviceId: {}", request.getUserId(),
				request.getDeviceId(), e);
		} catch (FeignException e) {
			log.error("Hackle 사용자 식별정보 업데이트 Feign 에러 - memberId: {}, deviceId: {}", request.getUserId(),
				request.getDeviceId(), e);
		}
	}

	public void sendKakao(HackleKakaoRequest request) {
		List<List<HackleKakaoRequest.HackleKakaoUser>> userBatches = partitionBySize(request.getUsers(),
			HACKLE_BATCH_SIZE);

		userBatches.forEach(batch -> {
			try {
				hackleFeignClient.sendKakaoMessage(apiKey,
					HackleRequest.from(
						request.getCampaignSpec().getCampaignKey(),
						batch
					));
			} catch (CustomRuntimeException e) {
				log.error(
					"Hackle 카카오 알림톡 발송 실패 - campaignKey: {}, code: {}", request.getCampaignSpec().getCampaignKey(),
					e.getCustomException().getCode()
				);
			} catch (FeignException e) {
				log.error(
					"Hackle 카카오 알림톡 발송 Feign 에러 - campaignKey: {}",
					request.getCampaignSpec().getCampaignKey(),
					e
				);
			}
		});
	}

	public static List<List<HackleKakaoRequest.HackleKakaoUser>> partitionBySize(
		List<HackleKakaoRequest.HackleKakaoUser> users, int batchSize) {
		List<List<HackleKakaoRequest.HackleKakaoUser>> batches = new ArrayList<>();
		for (int i = 0; i < users.size(); i += batchSize) {
			batches.add(new ArrayList<>(users.subList(i, Math.min(i + batchSize, users.size()))));
		}
		return batches;
	}
}
