package com.motd.be.module.admin.auth.service;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.module.admin.jwt.JwtAdminProvider.*;

import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.AdminAuthException;
import com.motd.be.module.admin.admin.entity.Admin;
import com.motd.be.module.admin.admin.service.AdminQueryService;
import com.motd.be.module.admin.auth.dto.request.AuthAdminSignInRequest;
import com.motd.be.module.admin.auth.dto.response.AuthAdminSignInResponse;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.redis.domain.repository.RedisBlackListRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthAdminService {

	private final AdminQueryService adminQueryService;
	private final BCryptPasswordEncoder encoder;
	private final RedisBlackListRepository redisBlackListUtil;

	public AuthAdminSignInResponse signIn(AuthAdminSignInRequest authAdminSignInRequest) {
		Admin admin = adminQueryService.findByEmail(authAdminSignInRequest.getEmail());

		if (!encoder.matches(authAdminSignInRequest.getPassword(), admin.getPassword())) {
			throw new CustomRuntimeException(AdminAuthException.INVALID_PASSWORD);
		}

		Map<String, Object> claims = Map.of(ID, admin.getId(), EMAIL, admin.getEmail(), ROLE,
			Role.ADMIN.getRoleType());

		return AuthAdminSignInResponse.from(createAdminTokens(claims));
	}

	@Transactional
	public void signOut(String accessToken) {
		redisBlackListUtil.setBlackListForSignOut(accessToken);
	}
}
