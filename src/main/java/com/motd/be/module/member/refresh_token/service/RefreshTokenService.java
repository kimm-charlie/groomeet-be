package com.motd.be.module.member.refresh_token.service;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.module.member.jwt.JwtProvider.*;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.AuthException;
import com.motd.be.exception.exceptions.RefreshTokenException;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.refresh_token.entity.RefreshToken;
import com.motd.be.redis.domain.repository.RedisAccessTokenRepository;
import com.motd.be.redis.domain.repository.RedisRefreshTokenRepository;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private final RefreshTokenCommandService refreshTokenCommandService;
	private final RedisAccessTokenRepository redisAccessTokenUtil;
	private final RefreshTokenQueryService refreshTokenQueryService;
	private final RedisRefreshTokenRepository redisRefreshTokenRepository;
	private final MemberQueryService memberQueryService;

	public Jwt reissueTokens(Member member, String oldAccessToken, String oldRefreshToken) {
		Jwt newToken = createTokens(Map.of(
			ID, member.getId(),
			ROLE, member.getRole().getRoleType()
		));

		refreshTokenCommandService.deleteByToken(oldRefreshToken);
		refreshTokenCommandService.save(RefreshToken.of(member, newToken));

		redisAccessTokenUtil.deleteAccessTokenByMemberId(member.getId(), oldAccessToken);
		redisAccessTokenUtil.saveAccessToken(member.getId(), newToken.getAccessToken());

		return newToken;
	}

	public Jwt issueTokens(Member member) {
		Jwt jwt = createTokens(Map.of(
			ID, member.getId(),
			ROLE, member.getRole().getRoleType()
		));

		refreshTokenCommandService.save(RefreshToken.of(member, jwt));
		redisAccessTokenUtil.saveAccessToken(member.getId(), jwt.getAccessToken());

		return jwt;
	}

	public void deleteRefreshTokens(Long memberId) {
		refreshTokenCommandService.deleteByMemberId(memberId);
	}

	/**
	 * RefreshToken 유효성 검증 및 재발급
	 * - 토큰 클레임 파싱
	 * - DB 조회 (비관락)
	 * - 실패 시 예외 처리 및 정리
	 */
	public Jwt validateAndReissueRefreshToken(String refreshTokenValue) {
		try {
			Claims claims = getClaimsFromRefreshToken(refreshTokenValue);
			Long memberId = Long.parseLong(String.valueOf(claims.get(ID)));
			Member member = memberQueryService.findById(memberId);

			if (member.getIsBanned()) {
				throw new CustomRuntimeException(RefreshTokenException.BANNED_MEMBER);
			}
			// DB 비관락으로 조회 시도
			return refreshTokenQueryService.findByMemberIdAndTokenWithLockOptional(memberId, refreshTokenValue)
				.map(refreshToken -> {
					// DB에 존재 -> 새로운 토큰 발급
					Jwt reissuedToken = issueTokens(member);

					// Redis에 재발급 토큰 저장
					redisRefreshTokenRepository.saveReissuedToken(refreshTokenValue, reissuedToken);

					// 기존 RefreshToken 삭제
					refreshTokenCommandService.deleteByToken(refreshTokenValue);

					return reissuedToken;
				})
				.orElseGet(() -> {
					// DB에 없음 -> Redis에서 재발급된 토큰 조회
					Jwt alreadyIssuedRefreshToken = redisRefreshTokenRepository.findReissuedToken(refreshTokenValue);

					if (alreadyIssuedRefreshToken != null) {
						return alreadyIssuedRefreshToken;
					}

					throw new CustomRuntimeException(RefreshTokenException.NOT_FOUND);
				});

		} catch (CustomRuntimeException e) {
			if (e.getCustomException() == RefreshTokenException.BANNED_MEMBER) {
				throw e; // 밴된 회원은 그대로 던짐
			}
			throw new CustomRuntimeException(AuthException.FAIL_TO_REISSUE_BY_NOT_EXISTING_TOKEN);
		} catch (RuntimeException e) {
			refreshTokenCommandService.deleteByToken(refreshTokenValue);
			throw new CustomRuntimeException(AuthException.FAIL_TO_REISSUE);
		}
	}
}
