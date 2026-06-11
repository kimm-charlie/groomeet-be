package com.motd.be.redis.domain.sign_In_bridge_code.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.SignInBridgeCodeException;
import com.motd.be.redis.domain.sign_In_bridge_code.entity.SignInBridgeCode;
import com.motd.be.redis.domain.sign_In_bridge_code.repository.SignInBridgeCodeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignInBridgeCodeService {

	private final SignInBridgeCodeRepository signInBridgeCodeRepository;

	public SignInBridgeCode findByUuid(String uuid) {
		return signInBridgeCodeRepository.findById(uuid).orElseThrow(() -> new CustomRuntimeException(
			SignInBridgeCodeException.NOT_FOUND));
	}

	public SignInBridgeCode save(SignInBridgeCode SignInBridgeCode) {
		return signInBridgeCodeRepository.save(SignInBridgeCode);
	}

	public void delete(String uuid) {
		signInBridgeCodeRepository.deleteById(uuid);
	}
}
