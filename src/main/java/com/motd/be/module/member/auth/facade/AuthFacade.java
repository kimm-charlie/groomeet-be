package com.motd.be.module.member.auth.facade;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.module.member.jwt.JwtProvider.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.apple_refresh_token.service.AppleRefreshTokenService;
import com.motd.be.module.member.auth.ClientType;
import com.motd.be.module.member.auth.dto.request.AuthReissueTokenRequest;
import com.motd.be.module.member.auth.dto.request.AuthSignOutRequest;
import com.motd.be.module.member.auth.dto.request.AuthSignUpRequest;
import com.motd.be.module.member.auth.dto.request.AuthWithdrawalRequest;
import com.motd.be.module.member.auth.dto.response.AuthExchangeCodeForTokenResponse;
import com.motd.be.module.member.auth.dto.response.AuthGenerateBridgeCodeResponse;
import com.motd.be.module.member.auth.dto.response.AuthReissueResponse;
import com.motd.be.module.member.auth.dto.response.AuthSignUpResponse;
import com.motd.be.module.member.auth.service.AuthService;
import com.motd.be.module.member.auth.validator.AuthValidator;
import com.motd.be.module.member.code_usage_history.service.CodeUsageHistoryService;
import com.motd.be.module.member.fcm_token.service.FcmTokenCommandService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberCommandService;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.member.service.MemberService;
import com.motd.be.module.member.member_block.service.MemberBlockCommandService;
import com.motd.be.module.member.member_director_favorite.service.MemberDirectorFavoriteCommandService;
import com.motd.be.module.member.notification.service.NotificationCommandService;
import com.motd.be.module.member.portfolio.service.PortfolioService;
import com.motd.be.module.member.refresh_token.service.RefreshTokenCommandService;
import com.motd.be.module.member.refresh_token.service.RefreshTokenService;
import com.motd.be.module.member.service_estimate.service.ServiceEstimateService;
import com.motd.be.module.member.service_estimate_template.service.ServiceEstimateTemplateCommandService;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.service.ServiceRequestService;
import com.motd.be.redis.domain.repository.RedisAccessTokenRepository;
import com.motd.be.redis.domain.repository.RedisBlackListRepository;
import com.motd.be.redis.domain.sign_In_bridge_code.entity.SignInBridgeCode;
import com.motd.be.redis.domain.sign_up_information.entity.SignUpInformation;
import com.motd.be.redis.domain.sign_up_information.service.SignUpInformationService;
import com.motd.be.shared.hackle.dto.request.HackleDeletePhoneNumberRequest;
import com.motd.be.shared.hackle.dto.request.HackleUpdateKakaoSubscriptionRequest;
import com.motd.be.shared.hackle.dto.request.HackleUpdatePushSubscriptionRequest;
import com.motd.be.shared.hackle.service.HackleEventPublisher;
import com.motd.be.shared.hackle.service.HackleService;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthFacade {

	private final AuthService authService;
	private final SignUpInformationService signUpInformationService;
	private final AuthValidator authValidator;
	private final MemberService memberService;
	private final RefreshTokenService refreshTokenService;
	private final AppleRefreshTokenService appleRefreshTokenService;
	private final RefreshTokenCommandService refreshTokenCommandService;
	private final RedisAccessTokenRepository redisAccessTokenUtil;
	private final RedisBlackListRepository redisBlackListUtil;
	private final MemberQueryService memberQueryService;
	private final ServiceRequestService serviceRequestService;
	private final ServiceEstimateService serviceEstimateService;
	private final MemberCommandService memberCommandService;
	private final NotificationCommandService notificationCommandService;
	private final MemberDirectorFavoriteCommandService memberDirectorFavoriteCommandService;
	private final MemberBlockCommandService memberBlockCommandService;
	private final PortfolioService portfolioService;
	private final ServiceEstimateTemplateCommandService serviceEstimateTemplateCommandService;
	private final CodeUsageHistoryService codeUsageHistoryService;
	private final HackleEventPublisher hackleEventPublisher;
	private final HackleService hackleService;
	private final FcmTokenCommandService fcmTokenCommandService;

	@Transactional
	public AuthSignUpResponse signUp(AuthSignUpRequest request, ClientType clientType) {
		// 1. 임시 가입정보 조회
		SignUpInformation signUpInformation = signUpInformationService.findByUuid(request.getUuid());
		authValidator.validateIdentifier(signUpInformation);

		// 3. 회원 + 약관 생성
		Member member = memberService.createMember(request, signUpInformation);

		// 가입 추천인 코드 사용 기록
		codeUsageHistoryService.useReferralCode(request.getReferralCode(), member);

		// 4. 토큰 발급 및 저장
		Jwt jwt = refreshTokenService.issueTokens(member);

		// 5. Apple 계정이면 RefreshToken 저장
		appleRefreshTokenService.saveAppleRefreshTokenIfNeeded(member, signUpInformation, clientType);

		// 6. 임시 가입정보 삭제
		signUpInformationService.delete(request.getUuid());

		// 7. hackle 에 사용자 정보 동기화
		hackleEventPublisher.updateKakaoSubscription(HackleUpdateKakaoSubscriptionRequest.from(member));
		hackleEventPublisher.updatePushSubscription(HackleUpdatePushSubscriptionRequest.from(member));

		return AuthSignUpResponse.of(jwt, member);
	}

	@Transactional
	public void signOut(Long memberId, String accessToken, String refreshToken,
		ClientType clientType, AuthSignOutRequest request) {

		String tokenToDelete = authService.resolveRefreshToken(clientType, refreshToken, request);

		// 1. Refresh Token 삭제
		refreshTokenCommandService.deleteByMemberIdAndRefreshToken(memberId, tokenToDelete);

		// 2. Redis Access Token 삭제
		redisAccessTokenUtil.deleteAccessTokenByMemberId(memberId, accessToken);

		// 3. Access Token 블랙리스트 등록
		redisBlackListUtil.setBlackListForSignOut(accessToken);
	}

	@Transactional
	public void withdrawal(Long memberId, AuthWithdrawalRequest request, String accessToken) {
		// 회원 조회
		Member member = memberQueryService.findByIdWithLock(memberId);

		// 모든 요청 취소 상태로 전환
		List<ServiceRequest> serviceRequests = serviceRequestService.cancelAllByMember(member);

		//요청와 연관된 제안 취소 처리
		serviceEstimateService.cancelAllByServiceRequests(serviceRequests);

		// 채팅방 나감 처리
		memberCommandService.leaveAllChatRoomsByMember(member);

		// todo 리뷰 삭제 처리 (추후 말해보고)

		// 알림 삭제 처리
		notificationCommandService.deleteAllByReceiver(member);

		// 즐겨찾기 삭제 처리
		memberDirectorFavoriteCommandService.deleteAllByMemberId(memberId);

		// 차단 삭제 처리
		memberBlockCommandService.deleteAllByMemberId(memberId);

		if (member.getIsDirector()) {
			// 디렉터용 포트폴리오 및 사진 삭제 처리
			portfolioService.deleteAllByDirectorInfo(member.getDirectorInfo());

			// 디렉터용 제안 취소 처리
			serviceEstimateService.cancelAllByDirectorInfo(member.getDirectorInfo());

			// 디렉터용 제안서 템플릿 삭제 처리
			serviceEstimateTemplateCommandService.deleteAllByDirectorInfo(member.getDirectorInfo());
		}

		// fcm 토큰 삭제 처리
		fcmTokenCommandService.unmapAllFcmTokensFromMember(member);

		// 해클 휴대폰 언매핑
		hackleEventPublisher.deletePhoneNumber(HackleDeletePhoneNumberRequest.builder().userId(member.getId()).build());

		// 회원 탈퇴 정보 업데이트
		memberService.updateWithdrawalInfo(member, request);

		// Refresh Token 삭제
		refreshTokenService.deleteRefreshTokens(memberId);

		// Apple 계정일 경우, Refresh Token Revoke
		appleRefreshTokenService.revokeAppleRefreshToken(member);

		// 현재 Access Token 블랙리스트 등록
		redisBlackListUtil.setBlackListForSignOut(accessToken);

		// Redis 내 모든 AccessToken 블랙리스트 처리 및 삭제
		authService.handleAllAccessTokensToBlackList(memberId);
	}

	@Transactional
	public AuthReissueResponse reissueToken(String refreshTokenFromCookie,
		ClientType clientType, AuthReissueTokenRequest request) {

		String refreshTokenValue = authService.resolveRefreshToken(clientType, refreshTokenFromCookie, request);

		// 1. RefreshToken 재발급
		Jwt reissuedToken = refreshTokenService.validateAndReissueRefreshToken(refreshTokenValue);

		return AuthReissueResponse.from(reissuedToken);
	}

	@Transactional
	public AuthGenerateBridgeCodeResponse generateBridgeCode(String accessToken) {
		return authService.generateBridgeCode(accessToken);
	}

	@Transactional
	public AuthExchangeCodeForTokenResponse exchangeCodeForToken(String code) {
		// 1. 브리지 코드 조회 및 삭제
		SignInBridgeCode bridgeCode = authService.findAndDeleteBridgeCode(code);

		// 2. 만료된 Access Token 에서 Claims 추출
		String accessToken = bridgeCode.getAccessToken();

		//todo 이거 만료되도 토큰 만들게 할건지?
		Claims claims = getClaimsFromExpiredAccessToken(accessToken);

		// 3. Claims 기반으로 새 토큰 발급
		Long memberId = ((Number)claims.get(ID)).longValue();
		Member member = memberQueryService.findById(memberId);
		Jwt issuedToken = refreshTokenService.issueTokens(memberQueryService.findById(member.getId()));

		return AuthExchangeCodeForTokenResponse.from(issuedToken);
	}

}
