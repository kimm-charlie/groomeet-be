package com.motd.be.shared.hackle.dto.request;

import java.util.List;
import java.util.Map;

import com.motd.be.shared.hackle.entity.HackleCampaignSpec;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HackleKakaoRequest {

	private final HackleCampaignSpec campaignSpec;
	private final Long senderId;
	private final Long referenceId;
	private final List<HackleKakaoUser> users;

	public static HackleKakaoRequest of(HackleCampaignSpec campaignSpec, Long senderId, Long receiverId,
		Long referenceId, Map<String, Object> variables) {
		return HackleKakaoRequest.builder()
			.campaignSpec(campaignSpec)
			.senderId(senderId)
			.referenceId(referenceId)
			.users(List.of(HackleKakaoUser.of(receiverId, variables)))
			.build();
	}

	@Getter
	@Builder
	public static class HackleKakaoUser {
		private final Long receiverId;
		private final Map<String, Object> variables;

		public static HackleKakaoUser of(Long receiverId, Map<String, Object> variables) {
			return HackleKakaoUser.builder().receiverId(receiverId).variables(variables).build();
		}
	}
}
