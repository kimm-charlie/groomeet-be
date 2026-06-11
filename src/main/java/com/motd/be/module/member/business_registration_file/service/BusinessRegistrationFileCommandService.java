package com.motd.be.module.member.business_registration_file.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.business_registration_file.entity.BusinessRegistrationFile;
import com.motd.be.module.member.business_registration_file.repository.BusinessRegistrationFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BusinessRegistrationFileCommandService {

	private final BusinessRegistrationFileRepository businessRegistrationFileRepository;

	public BusinessRegistrationFile save(BusinessRegistrationFile businessRegistrationFile) {
		return businessRegistrationFileRepository.save(businessRegistrationFile);
	}
}
