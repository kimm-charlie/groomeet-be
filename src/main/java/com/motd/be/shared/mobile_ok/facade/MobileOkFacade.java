package com.motd.be.shared.mobile_ok.facade;

import static com.motd.be.common.utils.MobileOkUtils.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.member.service.MemberService;
import com.motd.be.shared.hackle.dto.request.HackleUpdatePhoneNumberRequest;
import com.motd.be.shared.hackle.service.HackleEventPublisher;
import com.motd.be.shared.mobile_ok.dto.response.MobileOkCreateTokenResponse;
import com.motd.be.shared.mobile_ok.dto.response.MobileOkResultDto;
import com.motd.be.shared.mobile_ok.service.MobileOkCryptoService;
import com.motd.be.shared.mobile_ok.service.MobileOkService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MobileOkFacade {

	private final MobileOkCryptoService cryptoService;
	private final MobileOkService mobileOkService;
	private final MemberService memberService;
	private final HackleEventPublisher hackleEventPublisher;

	@Transactional
	public MobileOkCreateTokenResponse createMobileOkToken(Long memberId) {
		// 1. TxID 생성 및 저장
		String clientTxId = mobileOkService.createTxId(memberId);

		// 3. 암호화
		String encrypt = cryptoService.encryptReqClientInfo(buildReqClientInfo(clientTxId));

		// 4. response 빌드
		return mobileOkService.buildRequestResponse(encrypt, cryptoService.getServiceId());
	}

	@Transactional
	public MobileOkCreateTokenResponse createMobileOkTokenForWeb(Long memberId) {
		// 1. TxID 생성 및 저장
		String clientTxId = mobileOkService.createTxId(memberId);

		// 3. 암호화
		String encrypt = cryptoService.encryptReqClientInfo(buildReqClientInfo(clientTxId));

		// 4. response 빌드
		return mobileOkService.buildRequestResponseForWeb(encrypt, cryptoService.getServiceId());
	}

	@Transactional
	public void processResultForWeb(Long memberId, String data) {
		// 1. token 디코딩
		String decodedData = URLDecoder.decode(data, StandardCharsets.UTF_8);

		// 2. 토큰을 통해 본인인증 결과 처리
		MobileOkResultDto mobileOkResultDto = mobileOkService.processResult(decodedData);

		// 회원 업데이트
		memberService.updateAuthenticationInfo(memberId, mobileOkResultDto);

		// 3. clientTxId 검증 및 redis 정보 삭제
		mobileOkService.verifyClientTxId(memberId, mobileOkResultDto.getClientTxId());

		// 해클 본인인증 정보 업데이트
		hackleEventPublisher.updatePhoneNumber(
			HackleUpdatePhoneNumberRequest.of(memberId, mobileOkResultDto.getUserPhone()));
	}

	@Transactional
	public void processResultForApp(String mobileOkAuthToken, String data) {
		// 회원 아이디 조회
		Long memberId = mobileOkService.getMemberIdFromMobileOkAuthToken(mobileOkAuthToken);

		// 1. token 디코딩
		String decodedData = URLDecoder.decode(data, StandardCharsets.UTF_8);

		// 2. 토큰을 통해 본인인증 결과 처리
		MobileOkResultDto mobileOkResultDto = mobileOkService.processResult(decodedData);

		// 회원 업데이트
		memberService.updateAuthenticationInfo(memberId, mobileOkResultDto);

		// 3. clientTxId 검증 및 redis 정보 삭제
		mobileOkService.verifyClientTxId(memberId, mobileOkResultDto.getClientTxId());

		// 해클 본인인증 정보 업데이트
		hackleEventPublisher.updatePhoneNumber(
			HackleUpdatePhoneNumberRequest.of(memberId, mobileOkResultDto.getUserPhone()));

		// mobileOkAuthToken 삭제
		mobileOkService.deleteMobileOkAuthToken(mobileOkAuthToken);
	}
}
