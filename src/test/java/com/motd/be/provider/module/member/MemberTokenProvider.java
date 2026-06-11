package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;
import static com.motd.be.module.member.jwt.JwtProvider.*;

import java.util.Map;

import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Role;

public class MemberTokenProvider {

	public static Jwt generateTokenWithMemberIdRoleMember(Long memberId) {
		return createTokens(Map.of(ID_STR, memberId, ROLE_STR, Role.MEMBER.getRoleType()));
	}

	public static Jwt generateTokenWithMemberIdRoleDirector(Long memberId) {
		return createTokens(Map.of(ID_STR, memberId, ROLE_STR, Role.DIRECTOR.getRoleType()));
	}

	public static Jwt generateExpiredTokenWithMemberIdRoleMember(Long memberId) {
		return createExpiredTokens(Map.of(ID_STR, memberId, ROLE_STR, Role.MEMBER.getRoleType()));
	}
}
