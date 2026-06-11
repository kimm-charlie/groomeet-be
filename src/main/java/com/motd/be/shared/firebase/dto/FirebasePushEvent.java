package com.motd.be.shared.firebase.dto;

import java.util.List;
import java.util.Map;

import com.motd.be.shared.firebase.entity.FirebaseCampaignSpec;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FirebasePushEvent {

	private final FirebaseCampaignSpec campaignSpec;
	private final Long senderId;
	private final Long referenceId;
	private final List<Long> receiverIds;
	private final Map<String, String> variables;

	public static FirebasePushEvent of(
		FirebaseCampaignSpec campaignSpec,
		Long senderId,
		List<Long> receiverIds,
		Long referenceId,
		Map<String, String> variables
	) {
		return FirebasePushEvent.builder()
			.campaignSpec(campaignSpec)
			.senderId(senderId)
			.referenceId(referenceId)
			.receiverIds(receiverIds)
			.variables(variables)
			.build();
	}
}
