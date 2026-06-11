package com.motd.be.redis.domain.mobile_ok_information.entity;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("MobileOkInformation")
public class MobileOkInformation {

	@Id
	private String uuid;  // "memberId-uuid" 형태로 저장됨
	private String clientTxId;
	private String memberId;
	@TimeToLive
	private long timeToLive;

	@Builder
	public MobileOkInformation(String memberId, String clientTxId) {
		this.uuid = memberId + "-" + UUID.randomUUID().toString().replaceAll("-", "");
		this.clientTxId = clientTxId;
		this.timeToLive = 3600; // 기본 TTL 설정 (1시간)
	}

	public static MobileOkInformation of(Long memberId, String clientTxId) {
		return MobileOkInformation.builder()
			.memberId(String.valueOf(memberId))
			.clientTxId(clientTxId)
			.build();
	}
}
