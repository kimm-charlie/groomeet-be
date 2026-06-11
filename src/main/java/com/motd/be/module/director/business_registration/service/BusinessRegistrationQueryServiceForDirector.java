package com.motd.be.module.director.business_registration.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.business_registration.repository.BusinessRegistrationRepositoryForDirector;
import com.motd.be.module.member.business_registration.entity.BusinessRegistration;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessRegistrationQueryServiceForDirector {

	private final BusinessRegistrationRepositoryForDirector repository;

	public Optional<BusinessRegistration> findByMember(Member director) {
		return repository.findByMember(director);
	}
}
