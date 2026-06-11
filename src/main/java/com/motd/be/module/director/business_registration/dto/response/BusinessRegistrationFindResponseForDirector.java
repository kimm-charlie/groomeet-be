package com.motd.be.module.director.business_registration.dto.response;

import java.util.List;

import com.motd.be.module.director.file.dto.response.FileResponseForDirector;
import com.motd.be.module.member.business_registration.entity.BusinessRegistration;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BusinessRegistrationFindResponseForDirector {

	private Long id;
	private String businessRegistrationNumber;
	private String residentRegistrationNumber;
	private List<FileResponseForDirector> files;

	public static BusinessRegistrationFindResponseForDirector from(BusinessRegistration businessRegistration,
		String decryptedResidentRegistrationNumber) {
		return BusinessRegistrationFindResponseForDirector.builder()
			.id(businessRegistration.getId())
			.businessRegistrationNumber(businessRegistration.getBusinessRegistrationNumber())
			.residentRegistrationNumber(decryptedResidentRegistrationNumber)
			.files(FileResponseForDirector.fromListWithBusinessRegistrationFiles(businessRegistration.getFiles()))
			.build();
	}
}
