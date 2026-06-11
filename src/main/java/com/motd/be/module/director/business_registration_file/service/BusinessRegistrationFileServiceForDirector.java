package com.motd.be.module.director.business_registration_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.business_registration.entity.BusinessRegistration;
import com.motd.be.module.member.business_registration_file.entity.BusinessRegistrationFile;
import com.motd.be.module.member.business_registration_file.validator.BusinessRegistrationFileValidator;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessRegistrationFileServiceForDirector {

	private final BusinessRegistrationFileQueryServiceForDirector businessRegistrationFileQueryServiceForDirector;
	private final BusinessRegistrationFileCommandServiceForDirector businessRegistrationFileCommandServiceForDirector;
	private final BusinessRegistrationFileValidator businessRegistrationFileValidator;

	public void mapRegistration(BusinessRegistration businessRegistration, List<Long> fileIds, Member director) {
		List<BusinessRegistrationFile> businessRegistrationFilesFromDb = businessRegistrationFileQueryServiceForDirector.findAllByIds(
			fileIds);

		businessRegistrationFileValidator.validateFileOwnership(businessRegistrationFilesFromDb, fileIds, director);

		businessRegistrationFileCommandServiceForDirector.mapBusinessRegistration(businessRegistrationFilesFromDb,
			businessRegistration);
	}
}
