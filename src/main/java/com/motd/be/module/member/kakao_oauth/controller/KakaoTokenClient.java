package com.motd.be.module.member.kakao_oauth.controller;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.motd.be.module.member.kakao_oauth.dto.response.KakaoTokenResponse;

@FeignClient(name = "kakaoTokenClient", url = "${kakao.token.url}")
public interface KakaoTokenClient {

	@PostMapping(value = "/oauth/token",
		consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	KakaoTokenResponse getAccessToken(@RequestBody MultiValueMap<String, String> form);
}
