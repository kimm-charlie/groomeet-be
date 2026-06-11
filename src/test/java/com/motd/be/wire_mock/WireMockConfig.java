package com.motd.be.wire_mock;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.*;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.github.tomakehurst.wiremock.WireMockServer;

@TestConfiguration
public class WireMockConfig {

	// 고정 포트 번호 사용
	private static final int WIRE_MOCK_PORT_NUMBER = 9998;

	@Bean(destroyMethod = "stop")
	public WireMockServer wireMockServer() {
		WireMockServer server = new WireMockServer(options().port(WIRE_MOCK_PORT_NUMBER));
		server.start(); // 명시적 시작

		return server;
	}
}
