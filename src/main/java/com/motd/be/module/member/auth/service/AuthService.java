package com.motd.be.module.member.auth.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.AuthException;
import com.motd.be.module.member.auth.ClientType;
import com.motd.be.module.member.auth.dto.request.AuthReissueTokenRequest;
import com.motd.be.module.member.auth.dto.request.AuthSignOutRequest;
import com.motd.be.module.member.auth.dto.response.AuthGenerateBridgeCodeResponse;
import com.motd.be.redis.domain.repository.RedisAccessTokenRepository;
import com.motd.be.redis.domain.repository.RedisBlackListRepository;
import com.motd.be.redis.domain.sign_In_bridge_code.entity.SignInBridgeCode;
import com.motd.be.redis.domain.sign_In_bridge_code.service.SignInBridgeCodeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthService {

	private final RedisAccessTokenRepository redisAccessTokenUtil;
	private final RedisBlackListRepository redisBlackListUtil;
	private final SignInBridgeCodeService signInBridgeCodeService;

	public String resolveRefreshToken(ClientType clientType, String refreshToken, AuthSignOutRequest request) {
		return switch (clientType) {
			case WEB -> refreshToken;
			case APP -> request.getRefreshToken();
		};
	}

	public void handleAllAccessTokensToBlackList(Long memberId) {
		List<String> accessTokens = redisAccessTokenUtil.getAllAccessTokensByMemberId(memberId);
		if (!accessTokens.isEmpty()) {
			redisAccessTokenUtil.deleteAllAccessTokenByMemberId(memberId);
			accessTokens.forEach(redisBlackListUtil::setBlackListForSignOut);
		}
	}

	public String resolveRefreshToken(ClientType clientType, String refreshTokenFromCookie,
		AuthReissueTokenRequest request) {
		return switch (clientType) {
			case WEB -> {
				if (refreshTokenFromCookie == null)
					throw new CustomRuntimeException(AuthException.REFRESH_TOKEN_NOT_EXIST_IN_COOKIE);
				yield refreshTokenFromCookie;
			}
			case APP -> {
				if (request.getRefreshToken() == null)
					throw new CustomRuntimeException(AuthException.REFRESH_TOKEN_NOT_EXIST_IN_REQUEST);
				yield request.getRefreshToken();
			}
		};
	}

	public AuthGenerateBridgeCodeResponse generateBridgeCode(String accessToken) {
		return AuthGenerateBridgeCodeResponse.from(signInBridgeCodeService.save(SignInBridgeCode.of(accessToken)));
	}

	public SignInBridgeCode findAndDeleteBridgeCode(String code) {
		SignInBridgeCode signInBridgeCode = signInBridgeCodeService.findByUuid(code);
		signInBridgeCodeService.delete(code);
		return signInBridgeCode;
	}
}
