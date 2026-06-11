package com.motd.be.module.member.business_registration.service;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessRegistrationCryptoService {

	private final StringEncryptor stringEncryptor;

	public String encryptResidentRegistrationNumber(String plainText) {
		return stringEncryptor.encrypt(plainText);
	}

	public String decryptResidentRegistrationNumber(String encryptedText) {
		return stringEncryptor.decrypt(encryptedText);
	}
}
