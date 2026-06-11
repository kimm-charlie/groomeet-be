package com.motd.be.module.member.apple_refresh_token.service;

import static com.motd.be.common.constants.Constants.*;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.AppleOauthException;
import com.motd.be.exception.exceptions.AppleTokenException;
import com.motd.be.module.member.apple_oauth.service.AppleSecretKeyGenerator;
import com.motd.be.module.member.apple_refresh_token.entity.AppleRefreshToken;
import com.motd.be.module.member.auth.ClientType;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.redis.domain.sign_up_information.entity.SignUpInformation;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppleRefreshTokenService {

	private final AppleSecretKeyGenerator appleOauthSecretKeyGenerator;
	private final AppleRefreshTokenCommandService appleRefreshTokenCommandService;
	private final AppleRefreshTokenQueryService appleTokenQueryService;
	private String refreshTokenRevokeUrl;
	private String refreshTokenIssueUrl;
	@Value("${apple.client.url}")
	private String appleClientUrl;
	@Value("${apple.client-id-web}")
	private String APPLE_CLIENT_ID_WEB;
	@Value("${apple.client-id-app}")
	private String APPLE_CLIENT_ID_APP;

	@PostConstruct
	private void init() {
		refreshTokenRevokeUrl = appleClientUrl + APPLE_REVOKE_URL;
		refreshTokenIssueUrl = appleClientUrl + APPLE_ISSUE_URL;
	}

	public String getRefreshToken(String authCode, ClientType clientType) {
		String refreshToken = "";

		try {
			String clientSecret = appleOauthSecretKeyGenerator.createClientSecret(clientType);

			Map<String, String> params = new HashMap<>();
			params.put(CLIENT_SECRET, clientSecret); // 생성한 clientSecret
			params.put(CODE, authCode); // 애플 로그인 시, 응답값으로 받은 authorizationCode
			params.put(GRANT_TYPE, AUTHORIZATION_CODE);
			params.put(CLIENT_ID,
				clientType == ClientType.APP ? APPLE_CLIENT_ID_APP : APPLE_CLIENT_ID_WEB); // app bundle id

			HttpRequest getRequest = HttpRequest.newBuilder()
				.uri(new URI(refreshTokenIssueUrl))
				.POST(getParamsUrlEncoded(params))
				.headers(CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_FORM_URLENCODED))
				.build();

			HttpClient httpClient = HttpClient.newHttpClient();
			HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

			JSONObject parseData = new JSONObject(getResponse.body());
			refreshToken = parseData.get(REFRESH_TOKEN_UNDER_BAR).toString();

		} catch (Exception e) {
			throw new CustomRuntimeException(AppleOauthException.FAIL_TO_ISSUE_APPLE_REFRESH_TOKEN);
		}

		return refreshToken; // 생성된 refreshToken
	}

	public void saveAppleRefreshTokenIfNeeded(Member member, SignUpInformation signUpInformation,
		ClientType clientType) {
		if (signUpInformation.getPlatform().equals(SignInPlatform.APPLE)) {
			String appleRefresh = signUpInformation.getAttributes().get(APPLE_REFRESH_TOKEN);
			appleRefreshTokenCommandService.save(AppleRefreshToken.from(member, appleRefresh, clientType));
		}
	}

	public void revokeAppleRefreshToken(Member member) {
		if (!member.isSignInPlatformApple()) {
			return;
		}

		try {
			AppleRefreshToken appleToken = appleTokenQueryService.findByMember(member)
				.orElseThrow(() -> new CustomRuntimeException(AppleTokenException.NOT_FOUND));

			revokeRefreshToken(appleToken);
			appleRefreshTokenCommandService.deleteById(appleToken.getId());
		} catch (Exception e) {
			//revoke 에 실패했다 해서 회원탈퇴가 안되게 할순 없기때문에 이때는 logging 을 한다
			log.error("애플 회원탈퇴 실패 실패, memberId: {}, 이유: {}", member.getId(), e.getMessage());
		}
	}

	private HttpRequest.BodyPublisher getParamsUrlEncoded(Map<String, String> parameters) {
		String urlEncoded = parameters.entrySet()
			.stream()
			.map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
			.collect(Collectors.joining("&"));
		return HttpRequest.BodyPublishers.ofString(urlEncoded);
	}

	private void revokeRefreshToken(AppleRefreshToken appleToken) {
		try {
			ClientType clientType = appleToken.getClientType();
			String clientSecret = appleOauthSecretKeyGenerator.createClientSecret(clientType);

			Map<String, String> params = new HashMap<>();
			params.put(CLIENT_SECRET, clientSecret); // client_secret
			params.put(TOKEN, appleToken.getToken()); // refresh_token
			params.put(CLIENT_ID,
				appleToken.getClientType() == ClientType.APP ? APPLE_CLIENT_ID_APP : APPLE_CLIENT_ID_WEB);

			HttpRequest getRequest = HttpRequest.newBuilder()
				.uri(new URI(refreshTokenRevokeUrl))
				.POST(getParamsUrlEncoded(params))
				.headers(CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_FORM_URLENCODED))
				.build();

			HttpClient httpClient = HttpClient.newHttpClient();
			httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
		} catch (Exception e) {
			throw new CustomRuntimeException(AppleOauthException.FAIL_TO_REVOKE_APPLE_REFRESH_TOKEN);
		}
	}
}
