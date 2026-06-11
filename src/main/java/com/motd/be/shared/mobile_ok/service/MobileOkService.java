package com.motd.be.shared.mobile_ok.service;

import static com.motd.be.common.utils.MobileOkUtils.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.dreamsecurity.json.JSONObject;
import com.motd.be.exception.MobileOkCustomException;
import com.motd.be.redis.domain.mobile_ok_information.entity.MobileOkInformation;
import com.motd.be.redis.domain.mobile_ok_information.service.MobileOkInformationRedisService;
import com.motd.be.redis.domain.repository.RedisMobileOkRepository;
import com.motd.be.shared.mobile_ok.dto.response.MobileOkCreateTokenResponse;
import com.motd.be.shared.mobile_ok.dto.response.MobileOkResultDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MobileOkService {

	private final RedisMobileOkRepository redisMobileOkRepository;
	@Value("${mobile-ok.target-url}")
	private String targetUrl;
	@Value("${mobile-ok.return-url-app}")
	private String returnUrlForApp;
	@Value("${mobile-ok.return-url-web}")
	private String returnUrlForWeb;
	private final MobileOkInformationRedisService mobileOkInformationRedisService;
	private final MobileOkCryptoService mobileOkCryptoService;
	private final RestTemplate restTemplate;

	public MobileOkResultDto processResult(String token) {
		try {
			/* 1. 본인확인 인증결과 MOKToken API 요청 URL */
			JSONObject resultJSON = new JSONObject(token);
			String encryptMOKKeyToken = resultJSON.optString("encryptMOKKeyToken");

			// 로직 처리
			JSONObject requestData = new JSONObject();
			requestData.put("encryptMOKKeyToken", encryptMOKKeyToken);

			// 토큰 검증 요청
			String responseData = sendPostWithRestTemplate(targetUrl, requestData.toString());
			JSONObject responseJSON = new JSONObject(responseData);
			String encryptMOKResult = responseJSON.getString("encryptMOKResult");

			/* 3. 본인확인 결과 JSON 정보 파싱 */
			String resultJsonStr = mobileOkCryptoService.getMobileOK().getResultJSON(encryptMOKResult);
			return MobileOkResultDto.from(new JSONObject(resultJsonStr));
		} catch (Exception e) {
			log.error("MobileOK 본인인증 결과 처리 중 오류 발생", e);
			throw new MobileOkCustomException("본인인증 처리 중 오류가 발생했습니다. 다시 시도해 주세요.");
		}

	}

	public String sendPostWithRestTemplate(String dest, String jsonData) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(jsonData, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(dest, entity, String.class);
		return response.getBody();
	}

	public void verifyClientTxId(Long memberId, String clientTxId) {
		List<MobileOkInformation> mobileOkInformationList = mobileOkInformationRedisService.findAllByMemberId(memberId);

		mobileOkInformationList.stream()
			.filter(mobileOkInformation -> mobileOkInformation.getClientTxId().equals(clientTxId))
			.findFirst()
			.orElseThrow(() -> new MobileOkCustomException("ClientId 값이 일치하지 않습니다. 본인인증을 다시 시도해 주세요"));

		mobileOkInformationRedisService.deleteAllByMemberId(String.valueOf(memberId));
	}

	public MobileOkCreateTokenResponse buildRequestResponse(String encrypt, String serviceId) {
		return MobileOkCreateTokenResponse.from(encrypt, serviceId, returnUrlForApp);
	}

	public MobileOkCreateTokenResponse buildRequestResponseForWeb(String encrypt, String serviceId) {
		return MobileOkCreateTokenResponse.from(encrypt, serviceId, returnUrlForWeb);
	}

	public String createTxId(Long memberId) {
		String clientTxId = generateClientTxId();
		mobileOkInformationRedisService.save(MobileOkInformation.of(memberId, clientTxId));
		return clientTxId;
	}

	public void deleteMobileOkAuthToken(String mobileOkAuthToken) {
		if (mobileOkAuthToken == null) {
			return;
		}
		redisMobileOkRepository.deleteToken(mobileOkAuthToken);
	}

	public Long getMemberIdFromMobileOkAuthToken(String mobileOkAuthToken) {
		return redisMobileOkRepository.getMemberIdByToken(mobileOkAuthToken);
	}
}
