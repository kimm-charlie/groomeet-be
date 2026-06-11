package com.motd.be.common.config;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class JasyptConfig {

	@Primary
	@Bean("jasyptStringEncryptor")
	public StringEncryptor stringEncryptor() {

		Security.addProvider(new BouncyCastleProvider());

		String password = System.getenv("JASYPT_ENCRYPTOR_PASSWORD");
		PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
		SimpleStringPBEConfig config = new SimpleStringPBEConfig();

		config.setPassword(password);
		config.setAlgorithm("PBEWITHSHA256AND256BITAES-CBC-BC");
		config.setKeyObtentionIterations("10000");
		config.setProviderName("BC");
		config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
		config.setStringOutputType("base64");
		config.setPoolSize("1");

		encryptor.setConfig(config);
		return encryptor;
	}
}
