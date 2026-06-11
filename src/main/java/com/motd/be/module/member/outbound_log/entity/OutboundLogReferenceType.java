package com.motd.be.module.member.outbound_log.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OutboundLogReferenceType {

	MAIN,
	PORTFOLIO,
	REVIEW,
	CHAT_ROOM,
	SERVICE_ESTIMATE,
	CASH_PRODUCT,
	REQUEST_MARKET,
	BANNER_WEBVIEW;
}
