package com.motd.be.module.director.business_registration.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.business_registration.repository.BusinessRegistrationRepositoryForDirector;
import com.motd.be.module.member.business_registration.entity.BusinessRegistration;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BusinessRegistrationCommandServiceForDirector {

	private final BusinessRegistrationRepositoryForDirector businessRegistrationRepositoryForDirector;

	public BusinessRegistration save(BusinessRegistration businessRegistration) {
		return businessRegistrationRepositoryForDirector.save(businessRegistration);
	}
}
