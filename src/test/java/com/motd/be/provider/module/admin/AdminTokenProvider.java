package com.motd.be.provider.module.admin;

import static com.motd.be.Constants.*;
import static com.motd.be.module.admin.jwt.JwtAdminProvider.*;

import java.util.Map;

import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Role;

public class AdminTokenProvider {

	public static Jwt generateAdminTokenWithAdminId(Long adminId) {
		return createAdminTokens(Map.of(ID_STR, adminId, ROLE_STR, Role.ADMIN.getRoleType()));
	}
}
