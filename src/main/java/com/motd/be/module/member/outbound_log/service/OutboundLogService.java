package com.motd.be.module.member.outbound_log.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.shared.firebase.dto.FirebasePushEvent;
import com.motd.be.shared.firebase.dto.PushResult;
import com.motd.be.shared.firebase.entity.FirebaseOutboundLog;
import com.motd.be.shared.firebase.service.FirebaseOutboundLogCommandService;
import com.motd.be.shared.hackle.dto.request.HackleKakaoRequest.HackleKakaoUser;
import com.motd.be.shared.hackle.entity.HackleCampaignSpec;
import com.motd.be.shared.hackle.entity.HackleOutboundLog;
import com.motd.be.shared.hackle.service.HackleOutboundLogCommandService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboundLogService {

	private final FirebaseOutboundLogCommandService firebaseOutboundLogCommandService;
	private final HackleOutboundLogCommandService hackleOutboundLogCommandService;

	public void logFirebase(FirebasePushEvent request, PushResult result) {

		int targetCount = request.getReceiverIds().size();
		Long receiverId = targetCount == 1 ? request.getReceiverIds().get(0) : null;

		FirebaseOutboundLog log = FirebaseOutboundLog.of(request.getCampaignSpec(), receiverId, targetCount,
			result.getSuccessCount(), result.getFailureCount(),
			request.getSenderId(), request.getReferenceId());

		firebaseOutboundLogCommandService.save(log);
	}

	public void logHackle(HackleCampaignSpec campaignSpec, List<HackleKakaoUser> users, Long senderId,
		Long referenceId) {

		int targetCount = users.size();
		Long receiverId = targetCount == 1 ? users.get(0).getReceiverId() : null;

		HackleOutboundLog log = HackleOutboundLog.of(campaignSpec, receiverId, targetCount, senderId, referenceId);

		hackleOutboundLogCommandService.save(log);
	}
}
