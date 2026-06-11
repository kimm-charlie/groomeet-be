package com.motd.be.module.member.apple_oauth.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.auth.ClientType;

import io.jsonwebtoken.Jwts;

@Component
public class AppleSecretKeyGenerator {

	@Value("${apple.secret-key.url}")
	private String APPLE_URL;
	@Value("${APPLE_KEY_PATH}")
	private String APPLE_KEY_PATH;
	@Value("${apple.client-id-web}")
	private String APPLE_CLIENT_ID_WEB;
	@Value("${apple.client-id-app}")
	private String APPLE_CLIENT_ID_APP;
	@Value("${apple.secret-key.team-id}")
	private String APPLE_TEAM_ID;
	@Value("${apple.secret-key.key-id}")
	private String APPLE_KEY_ID;

	public String createClientSecret(ClientType clientType) throws IOException {
		Date expirationDate = Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant());
		Map<String, Object> jwtHeader = new HashMap<>();
		jwtHeader.put("kid", APPLE_KEY_ID); //key id
		jwtHeader.put("alg", "ES256");

		return Jwts.builder()
			.setHeaderParams(jwtHeader)
			.setIssuer(APPLE_TEAM_ID) //iss ( teamId
			.setIssuedAt(new Date(System.currentTimeMillis())) // 발행 시간 - UNIX 시간
			.setExpiration(expirationDate) // 만료 시간
			.setAudience(APPLE_URL)
			.setSubject(clientType == ClientType.APP ? APPLE_CLIENT_ID_APP : APPLE_CLIENT_ID_WEB) // sub (clientId)
			.signWith(getPrivateKey())
			.compact();
	}

	public PrivateKey getPrivateKey() throws IOException {
		FileSystemResource resource = new FileSystemResource(APPLE_KEY_PATH);
		InputStream in = resource.getInputStream();
		PEMParser pemParser = new PEMParser(new StringReader(IOUtils.toString(in, StandardCharsets.UTF_8)));
		PrivateKeyInfo object = (PrivateKeyInfo)pemParser.readObject();
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
		return converter.getPrivateKey(object);
	}

}
