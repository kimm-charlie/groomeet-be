package com.motd.be.module.member.service_estimate.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ServiceEstimateReminderStatus {
	PENDING("발송대기"), SENT("발송완료");

	private final String description;
}
