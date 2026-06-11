package com.motd.be.redis.domain.sign_up_information.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.SignUpInformationException;
import com.motd.be.redis.domain.sign_up_information.entity.SignUpInformation;
import com.motd.be.redis.domain.sign_up_information.repository.SignUpInformationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignUpInformationService {

	private final SignUpInformationRepository signUpInformationRepository;

	public SignUpInformation findByUuid(String uuid) {
		return signUpInformationRepository.findById(uuid).orElseThrow(() -> new CustomRuntimeException(
			SignUpInformationException.INVALID_UUID));
	}

	public void save(SignUpInformation signUpInformation) {
		signUpInformationRepository.save(signUpInformation);
	}

	public void delete(String uuid) {
		signUpInformationRepository.deleteById(uuid);
	}
}
