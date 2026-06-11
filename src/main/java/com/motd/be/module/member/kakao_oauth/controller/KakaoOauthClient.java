package com.motd.be.module.member.kakao_oauth.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.motd.be.module.member.kakao_oauth.dto.response.KakaoOauthMemberInfoResponse;

@FeignClient(name = "kakaoClient", url = "${kakao.client.url}")
public interface KakaoOauthClient {

	@GetMapping(value = "/v2/user/me")
	KakaoOauthMemberInfoResponse getKakaoMemberInfo(@RequestHeader(HEADER_AUTHORIZATION) String accessToken);

}
