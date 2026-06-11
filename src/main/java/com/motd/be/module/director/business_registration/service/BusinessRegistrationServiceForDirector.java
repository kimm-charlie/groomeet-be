package com.motd.be.module.director.business_registration.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.BusinessRegistrationException;
import com.motd.be.module.director.business_registration.dto.request.BusinessRegistrationCreateRequestForDirector;
import com.motd.be.module.director.business_registration.dto.response.BusinessRegistrationFindResponseForDirector;
import com.motd.be.module.member.business_registration.entity.BusinessRegistration;
import com.motd.be.module.member.business_registration.service.BusinessRegistrationCryptoService;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessRegistrationServiceForDirector {

	private final BusinessRegistrationCommandServiceForDirector businessRegistrationCommandServiceForDirector;
	private final BusinessRegistrationQueryServiceForDirector businessRegistrationQueryServiceForDirector;
	private final BusinessRegistrationCryptoService businessRegistrationCryptoService;

	public BusinessRegistration register(Member director, BusinessRegistrationCreateRequestForDirector request) {
		try {
			String encryptedResidentRegistrationNumber = businessRegistrationCryptoService.encryptResidentRegistrationNumber(
				request.getResidentRegistrationNumber());
			return businessRegistrationCommandServiceForDirector.save(
				request.toEntity(director, encryptedResidentRegistrationNumber));
		} catch (DataIntegrityViolationException e) {
			throw new CustomRuntimeException(BusinessRegistrationException.DUPLICATED_BUSINESS_REGISTRATION);
		}
	}

	public BusinessRegistrationFindResponseForDirector find(Member director) {
		return businessRegistrationQueryServiceForDirector.findByMember(director)
			.map(businessRegistration -> BusinessRegistrationFindResponseForDirector.from(businessRegistration,
				businessRegistrationCryptoService.decryptResidentRegistrationNumber(
					businessRegistration.getResidentRegistrationNumber())))
			.orElse(BusinessRegistrationFindResponseForDirector.builder().build());
	}
}
