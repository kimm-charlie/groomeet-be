package com.motd.be.module.member.auth.service;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;
import static com.motd.be.module.member.jwt.JwtProvider.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.MemberException;
import com.motd.be.module.member.auth.validator.AuthValidator;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.refresh_token.entity.RefreshToken;
import com.motd.be.module.member.refresh_token.service.RefreshTokenCommandService;
import com.motd.be.redis.domain.repository.RedisAccessTokenRepository;
import com.motd.be.redis.domain.sign_up_information.entity.SignUpInformation;
import com.motd.be.redis.domain.sign_up_information.service.SignUpInformationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthOauthService {

	private final MemberQueryService memberQueryService;
	private final SignUpInformationService signUpInformationService;
	private final RefreshTokenCommandService refreshTokenCommandService;
	private final RedisAccessTokenRepository redisAccessTokenUtil;
	private final AuthValidator authValidator;

	public Map<String, String> handleOauthProcess(SignInPlatform signInPlatform, String identifier,
		String email, Map<String, String> attributes) {
		Optional<Member> memberFoundByIdentifier = memberQueryService.findByIdentifierAndPlatform(identifier,
			signInPlatform);

		// 1. 회원 존재시
		if (memberFoundByIdentifier.isPresent()) {
			Member member = memberFoundByIdentifier.get();

			// 2. 회원 탈퇴한 identifier 라면, 예외를 던진다.
			if (member.getIsWithdrawal()) {
				throw new CustomRuntimeException(MemberException.WITHDRAWAL_HISTORY_EXIST);
			}

			// 3. 벤된 회원일 경우 예외를 던진다.
			if (member.getIsBanned()) {
				if (!member.getUnbannedAt().isBefore(LocalDate.now())) {
					return handleBannedMember(member);
				}
			}
			return handleSignIn(member);
		} else {
			// 회원이 존재하지 않는다. 즉 탈퇴한지 한달이 넘은 회원 또는 신규 회원이다.
			authValidator.validateDuplicateEmail(email);
			return handleSignUp(email, signInPlatform, identifier, attributes);
		}
	}

	private Map<String, String> handleBannedMember(Member member) {
		return Map.of(
			IS_EXISTING_MEMBER, TRUE,
			IS_BANNED, String.valueOf(member.getIsBanned()),
			UNBANNED_AT, formatToDateString(member.getUnbannedAt())
		);
	}

	public Map<String, String> handleSignIn(Member member) {
		Long memberId = member.getId();
		Role role = member.getRole();
		Jwt jwt = createTokens(Map.of(ID, memberId, ROLE, role.getRoleType()));

		redisAccessTokenUtil.saveAccessToken(member.getId(), jwt.getAccessToken());
		RefreshToken refreshToken = RefreshToken.of(member, jwt);
		refreshTokenCommandService.save(refreshToken);

		return Map.of(
			IS_EXISTING_MEMBER, TRUE,
			ACCESS_TOKEN, jwt.getAccessToken(),
			REFRESH_TOKEN, jwt.getRefreshToken(),
			ROLE, role.getRoleType(),
			IS_BANNED, String.valueOf(member.getIsBanned()),
			MEMBER_ID, String.valueOf(memberId)
		);
	}

	public Map<String, String> handleSignUp(String email, SignInPlatform signInPlatform,
		String identifier, Map<String, String> attributes) {
		String randomUUID = SIGN_UP + UUID.randomUUID();
		signUpInformationService.save(SignUpInformation.of(randomUUID, signInPlatform, identifier, email, attributes));

		return Map.of(
			IS_EXISTING_MEMBER, FALSE,
			UUID_KEY, randomUUID,
			IS_BANNED, String.valueOf(Boolean.FALSE)
		);
	}
}
