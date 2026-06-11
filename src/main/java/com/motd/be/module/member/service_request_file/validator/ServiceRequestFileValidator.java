package com.motd.be.module.member.service_request_file.validator;

import java.util.List;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ServiceRequestFileException;
import com.motd.be.module.member.service_request_file.entity.ServiceRequestFile;

@Component
public class ServiceRequestFileValidator {

	public void validateOwnership(List<ServiceRequestFile> files, Long memberId) {
		files.forEach(image -> {
			if (!image.isOwnedBy(memberId)) {
				throw new CustomRuntimeException(ServiceRequestFileException.NOT_OWNED_BY);
			}
		});
	}

	public void validateImageSize(List<ServiceRequestFile> imagesFromDb, List<Long> fileIdsFromRequest) {
		if (imagesFromDb.size() != fileIdsFromRequest.size()) {
			throw new CustomRuntimeException(ServiceRequestFileException.IMAGE_SIZE_MISMATCH);
		}
	}
}
