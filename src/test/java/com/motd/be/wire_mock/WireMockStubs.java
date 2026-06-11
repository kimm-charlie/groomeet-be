package com.motd.be.wire_mock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import com.github.tomakehurst.wiremock.WireMockServer;

public class WireMockStubs {

	/**
	 * 카카오 API 응답을 Stubbing하는 메서드
	 */
	public static void stubKakaoResponseUsingAccessToken(WireMockServer wireMockServer, String identifier,
		String email) {
		wireMockServer.stubFor(get(urlEqualTo("/v2/user/me"))
			.withHeader("Authorization", matching("Bearer .*"))
			.willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBody(String.format("""
					    {
					        "id": "%s",
					        "kakao_account": {
					            "email": "%s"
					        }
					    }
					""", identifier, email))));
	}

	/**
	 * 카카오 TOKEN API 응답을 Stubbing하는 메서드
	 *
	 * @param wireMockServer
	 * @param authorizationCode
	 * @param accessToken
	 * @param refreshToken
	 */
	public static void stubKakaoTokenResponse(WireMockServer wireMockServer,
		String authorizationCode,
		String accessToken,
		String refreshToken) {

		wireMockServer.stubFor(post(urlEqualTo("/oauth/token"))
			.withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
			.withRequestBody(containing("grant_type=authorization_code"))
			.withRequestBody(containing("code=" + authorizationCode))
			.willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBody(String.format("""
					{
					  "token_type": "bearer",
					  "access_token": "%s",
					  "expires_in": 21599,
					  "refresh_token": "%s",
					  "refresh_token_expires_in": 5183999,
					  "scope": "account_email profile"
					}
					""", accessToken, refreshToken))));
	}

	/**
	 * 애플 PUBLIC KEY API 응답을 Stubbing하는 메서드
	 */
	public static void stubApplePublicKeyResponse(WireMockServer wireMockServer, KeyPair keyPair, String kid,
		String alg) {
		RSAPublicKey publicKey = (RSAPublicKey)keyPair.getPublic(); // 공개키 추출
		String n = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getModulus().toByteArray());
		String e = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getPublicExponent().toByteArray());

		wireMockServer.stubFor(get(urlEqualTo("/auth/keys"))
			.willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBody(String.format("""
					    {
					        "keys": [
					            {
					                "kty": "RSA",
					                "kid": "%s",
					                "use": "sig",
					                "alg": "%s",
					                "n": "%s",
					                "e": "%s"
					            }
					        ]
					    }
					""", kid, alg, n, e))));
	}

	/**
	 * 애플 REFRESHTOKEN API 응답을 Stubbing하는 메서드
	 */
	public static void stubAppleRefreshTokenResponse(WireMockServer wireMockServer) {
		wireMockServer.stubFor(post(urlEqualTo("/auth/token"))
			.willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBody("""
					    {
					        "refresh_token": "refreshToken"
					    }
					""")));
	}

	/**
	 * 애플 REFRESHTOKEN revoke API 응답을 Stubbing하는 메서드
	 */
	public static void stubAppleRefreshTokenRevokeResponse(WireMockServer wireMockServer) {
		wireMockServer.stubFor(post(urlEqualTo("/auth/revoke"))
			.willReturn(aResponse()
				.withHeader("Content-Type", "application/json")));
	}

}
