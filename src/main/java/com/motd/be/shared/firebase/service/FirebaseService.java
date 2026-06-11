package com.motd.be.shared.firebase.service;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.utils.FirebaseCampaignTemplateUtils.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.motd.be.module.member.fcm_token.entity.FcmToken;
import com.motd.be.module.member.fcm_token.service.FcmTokenCommandService;
import com.motd.be.module.member.fcm_token.service.FcmTokenQueryService;
import com.motd.be.shared.firebase.dto.FirebasePushEvent;
import com.motd.be.shared.firebase.dto.PushResult;
import com.motd.be.shared.firebase.entity.FirebaseCampaignSpec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseService {

	private final FcmTokenCommandService fcmTokenCommandService;
	private final FcmTokenQueryService fcmTokenQueryService;

	public PushResult sendPushTo(FirebasePushEvent event) {
		List<FcmToken> fcmTokens = fcmTokenQueryService.findAllByMemberIdsWithIsActivityPushAgreed(
			event.getReceiverIds());

		if (fcmTokens.isEmpty()) {
			return PushResult.of(0, 0);
		}

		if (fcmTokens.size() == 1) {
			return sendSingleMessage(event, fcmTokens.get(0));
		} else {
			return sendMulticastMessage(event, fcmTokens);
		}
	}

	private PushResult sendMulticastMessage(FirebasePushEvent event, List<FcmToken> tokenInfos) {
		FirebaseCampaignSpec campaignSpec = event.getCampaignSpec();

		// 1. Firebase 전송용 token 문자열 리스트
		Map<String, List<FcmToken>> tokensByValue = new LinkedHashMap<>();
		for (FcmToken tokenInfo : tokenInfos) {
			tokensByValue.computeIfAbsent(tokenInfo.getToken(), key -> new ArrayList<>()).add(tokenInfo);
		}
		List<String> tokens = new ArrayList<>(tokensByValue.keySet());

		// title 및 body는 동일하므로 미리 렌더링
		String renderedTitle = renderTitle(event);
		String renderedBody = renderBody(event);

		MulticastMessage message = MulticastMessage.builder()
			.addAllTokens(tokens)
			.putData(REFERENCE_TYPE, campaignSpec.getReferenceType().name())
			.putData(REFERENCE_ID, String.valueOf(event.getReferenceId()))
			.putData(RECEIVER_TYPE, campaignSpec.getOutboundLogReceiverType().name())
			.putData(CLICK_ACTION, FLUTTER_NOTIFICATION_CLICK)
			.setNotification(Notification.builder().setTitle(renderedTitle).setBody(renderedBody).build())
			.setAndroidConfig(AndroidConfig.builder()
				.setNotification(AndroidNotification.builder().setClickAction(FLUTTER_NOTIFICATION_CLICK).build())
				.build())
			.build();

		try {
			BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

			if (response.getFailureCount() > 0) {

				// 2. 실패한 FcmTokenInfo 수집 (index 기반 매핑)
				List<FcmToken> failedTokenInfos = new ArrayList<>();

				List<SendResponse> responses = response.getResponses();
				for (int i = 0; i < responses.size(); i++) {
					if (!responses.get(i).isSuccessful()) {
						// The order of responses corresponds to the order of the registration tokens.
						failedTokenInfos.addAll(tokensByValue.get(tokens.get(i)));
					}
				}

				// 3. DB 처리 (ID 기준)
				fcmTokenCommandService.incrementFailedCountOrMarkAsDeletedForMultipleFcmTokenIds(
					failedTokenInfos.stream().map(FcmToken::getId).distinct().toList());
			}

			return PushResult.of(response.getSuccessCount(), response.getFailureCount());

		} catch (FirebaseMessagingException e) {
			log.error("firebase multicast failed", e);
			return PushResult.of(0, tokens.size());
		}
	}

	private PushResult sendSingleMessage(FirebasePushEvent event, FcmToken token) {
		FirebaseCampaignSpec campaignSpec = event.getCampaignSpec();

		Message message = Message.builder()
			.setToken(token.getToken())
			.putData(REFERENCE_TYPE, campaignSpec.getReferenceType().name())
			.putData(REFERENCE_ID, String.valueOf(event.getReferenceId()))
			.putData(RECEIVER_TYPE, campaignSpec.getOutboundLogReceiverType().name())
			.putData(CLICK_ACTION, FLUTTER_NOTIFICATION_CLICK)
			.setNotification(Notification.builder().setTitle(renderTitle(event)).setBody(renderBody(event)).build())
			.setAndroidConfig(AndroidConfig.builder()
				.setNotification(AndroidNotification.builder().setClickAction(FLUTTER_NOTIFICATION_CLICK).build())
				.build())
			.build();

		try {
			FirebaseMessaging.getInstance().send(message);
			return PushResult.of(1, 0);
		} catch (FirebaseMessagingException e) {
			log.error("firebase single message failed receiverId: {}, token: {}", event.getReceiverIds().get(0),
				token.getToken(), e);
			fcmTokenCommandService.incrementFailedCountOrMarkAsDeletedForSingleFcmTokenId(token.getId());
			return PushResult.of(0, 1);
		}
	}
}
