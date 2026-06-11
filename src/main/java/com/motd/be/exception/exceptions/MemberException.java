package com.motd.be.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.motd.be.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MemberException implements CustomException {

	WITHDRAWAL_HISTORY_EXIST(HttpStatus.BAD_REQUEST, "한달이내에 회원탈퇴를 한 기록이 있는 회원입니다.", "MEMBER_001"),
	IDENTIFIER_NOT_FOUND(HttpStatus.NOT_FOUND, "요청에 회원 고유 식별자가 없습니다.", "MEMBER_002"),
	NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을수 없습니다.", "MEMBER_003"),
	FAIL_TO_UPDATE(HttpStatus.BAD_REQUEST, "정보 업데이트에 실패했습니다.", "MEMBER_004"),
	DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "중복된 닉네임이 존재합니다.", "MEMBER_005"),
	PROFILE_IMAGE_NOT_OWNED(HttpStatus.FORBIDDEN, "프로필 이미지는 본인의 사진으로만 바꿀수 있습니다.", "MEMBER_006"),
	ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 역활을 찾을수 없습니다.", "MEMBER_007"),
	NOT_AUTHENTICATED(HttpStatus.FORBIDDEN, "본인인증된 회원이 아닙니다.", "MEMBER_008"),
	WITHDRAWAL_MEMBER(HttpStatus.NOT_FOUND, "탈퇴한 회원 입니다.", "MEMBER_009"),
	INVALID_REFERRAL_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 추천인 코드입니다.", "MEMBER_010"),
	DUPLICATED_EMAIL(HttpStatus.BAD_REQUEST, "같은 이메일로 가입한 기록이 있습니다.", "MEMBER_011"),
	DUPLICATED_AUTHENTICATION_CI(HttpStatus.BAD_REQUEST, "이미 본인인증을 완료한 이력이 있는 회원입니다.", "MEMBER_012");

	private final HttpStatus status;
	private final String message;
	private final String code;

	@Override
	public HttpStatus getHttpStatus() {
		return status;
	}

	@Override
	public String getErrorMessage() {
		return message;
	}

	@Override
	public String getName() {
		return name();
	}

	@Override
	public String getCode() {
		return code;
	}
}
