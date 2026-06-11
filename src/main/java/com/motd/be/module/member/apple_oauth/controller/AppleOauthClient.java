package com.motd.be.module.member.apple_oauth.controller;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.motd.be.module.member.apple_oauth.dto.response.AppleOauthPublicKeyResponse;

@FeignClient(name = "appleClient", url = "${apple.client.url}")
public interface AppleOauthClient {

	@GetMapping(value = "/auth/keys")
	AppleOauthPublicKeyResponse getAppleAuthPublicKey();

}
