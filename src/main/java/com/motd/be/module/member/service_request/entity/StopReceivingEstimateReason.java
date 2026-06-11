package com.motd.be.module.member.service_request.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StopReceivingEstimateReason {

	CONFIRMED_WITH_DIRECTOR("제안을 보낸 디렉터와 서비스 진행을 확정했습니다."),
	HIRED_FROM_OTHER_SERVICE("Groomeet이 아닌 다른 서비스로 디렉터님을 구했습니다."),
	SOLVED_PERSONALLY_OR_BY_ACQUAINTANCE("지인의 소개를 받거나 스스로 해결했습니다."),
	PLAN_CHANGED("계획이 변경되었습니다."),
	COULD_NOT_FIND_DIRECTOR("마음에 드는 디렉터님을 찾지 못했습니다."),
	ETC("기타");

	private final String description;
}
