package com.motd.be.module.member.business_registration_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.FileException;
import com.motd.be.module.member.business_registration_file.entity.BusinessRegistrationFile;
import com.motd.be.module.member.business_registration_file.repository.BusinessRegistrationFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessRegistrationFileQueryService {

	private final BusinessRegistrationFileRepository businessRegistrationFileRepository;

	public List<BusinessRegistrationFile> findAllByIds(List<Long> ids) {
		return businessRegistrationFileRepository.findAllByIds(ids);
	}

	public BusinessRegistrationFile findByFileKey(String fileKey) {
		return businessRegistrationFileRepository.findByFileKey(fileKey)
			.orElseThrow(() -> new CustomRuntimeException(FileException.FILE_NOT_FOUND));
	}
}
