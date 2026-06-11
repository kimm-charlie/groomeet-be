package com.motd.be.module.member.service_estimate_file.validator;

import java.util.List;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ServiceEstimateFileException;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateFile;

@Component
public class ServiceEstimateFileValidator {

	public void validateEstimateImages(Member member, List<ServiceEstimateFile> files,
		List<Long> requestedFileIds) {

		for (ServiceEstimateFile image : files) {
			// 소유자 검증
			if (!image.getMember().getId().equals(member.getId())) {
				throw new CustomRuntimeException(ServiceEstimateFileException.NOT_OWNED);
			}
		}

		// 개수 검증
		if (files.size() != requestedFileIds.size()) {
			throw new CustomRuntimeException(ServiceEstimateFileException.INVALID_IMAGE_COUNT);
		}
	}
}
