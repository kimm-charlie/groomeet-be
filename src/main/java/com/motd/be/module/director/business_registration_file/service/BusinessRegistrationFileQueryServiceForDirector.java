package com.motd.be.module.director.business_registration_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.business_registration_file.repository.BusinessRegistrationFileRepositoryForDirector;
import com.motd.be.module.member.business_registration_file.entity.BusinessRegistrationFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessRegistrationFileQueryServiceForDirector {

	private final BusinessRegistrationFileRepositoryForDirector businessRegistrationFileRepositoryForDirector;

	public List<BusinessRegistrationFile> findAllByIds(List<Long> ids) {
		return businessRegistrationFileRepositoryForDirector.findAllByIds(ids);
	}
}
