package com.motd.be.module.member.member.entity;

import static com.motd.be.common.constants.Constants.*;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.MemberException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
	MEMBER(ROLE_MEMBER),
	DIRECTOR(ROLE_DIRECTOR),
	ADMIN(ROLE_ADMIN);

	private final String roleType;

	public static Role from(String roleStr) {
		return switch (roleStr) {
			case ROLE_MEMBER -> MEMBER;
			case ROLE_DIRECTOR -> DIRECTOR;
			case ROLE_ADMIN -> ADMIN;
			default -> throw new CustomRuntimeException(MemberException.ROLE_NOT_FOUND);
		};
	}
}
