package com.motd.be.module.member.business_registration_file.validator;

import java.util.List;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.BusinessRegistrationFileException;
import com.motd.be.module.member.business_registration_file.entity.BusinessRegistrationFile;
import com.motd.be.module.member.member.entity.Member;

@Component
public class BusinessRegistrationFileValidator {

	public void validateFileOwnership(List<BusinessRegistrationFile> businessRegistrationFilesFromDb,
		List<Long> fileIds, Member director) {
		if (businessRegistrationFilesFromDb.size() != fileIds.size()) {
			throw new CustomRuntimeException(BusinessRegistrationFileException.INVALID_IMAGE_EXIST);
		}

		businessRegistrationFilesFromDb.forEach(file -> {
			if (!file.isOwnedBy(director.getId())) {
				throw new CustomRuntimeException(BusinessRegistrationFileException.NOT_OWNED_BY);
			}
		});
	}
}
