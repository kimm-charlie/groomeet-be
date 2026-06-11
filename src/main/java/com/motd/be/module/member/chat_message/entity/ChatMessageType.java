package com.motd.be.module.member.chat_message.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
/**
 * displaytext 는 채팅방 전체보기에서 표현이 되는 텍스트 이다.
 */
public enum ChatMessageType {

	TEXT(""),
	DOCUMENT("파일을 보냈습니다."),
	IMAGE("사진을 보냈습니다."),
	ESTIMATE("제안을 보냈습니다."),
	ESTIMATE_ACCEPTED("제안을 수락했습니다."),
	ESTIMATE_UPDATED("제안이 수정되었습니다."),
	ESTIMATE_COMPLETED_BY_DIRECTOR("디렉터가 작업을 완료했습니다."),
	ESTIMATE_COMPLETED_BY_MEMBER("요청인이 거래를 확정 했습니다."),
	ESTIMATE_CANCELED("제안이 파기되었습니다."),
	REVIEW_COMPLETED("리뷰가 작성되었습니다.");

	private final String displayText;
}
