package com.motd.be.module.member.director_service.validator;

import java.util.List;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.DirectorServiceException;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;

@Component
public class DirectorServiceValidator {

	/**
	 * 해당 서비스가 유효한 서비스인지 확인한다.
	 *
	 * @param directorServices
	 */
	public void validateAllHaveParent(List<DirectorService> directorServices) {
		directorServices.forEach(service -> {
			if (service.getParent() == null) {
				throw new CustomRuntimeException(DirectorServiceException.INVALID_SERVICE);
			}
		});
	}

	/**
	 * 서비스 등록시, DB에서 찾은 서비스의 갯수와 요청한 서비스의 갯수가 동일한지 확인한다.
	 *
	 * @param directorServices
	 */
	public void validateAllRequestedServicesExist(List<DirectorService> directorServices,
		List<Long> requestServiceIds) {
		if (directorServices.size() != requestServiceIds.size()) {
			throw new CustomRuntimeException(DirectorServiceException.INVALID_SERVICE);
		}
	}

	/**
	 * 요청된 서비스들이 모두 존재하며,
	 * 각 서비스가 유효한 부모 카테고리를 가지고 있는지 검증한다.
	 *
	 * @param directorServices  DB에서 조회한 서비스 목록
	 * @param requestServiceIds 요청된 서비스 ID 목록
	 */
	public void validateRequestedServices(List<DirectorService> directorServices, List<Long> requestServiceIds) {
		// 1. 요청한 서비스 수와 실제 존재하는 서비스 수 일치 여부 검증
		if (directorServices.size() != requestServiceIds.size()) {
			throw new CustomRuntimeException(DirectorServiceException.INVALID_SERVICE);
		}

		// 2. 각 서비스가 부모 카테고리를 가지고 있는지 검증
		boolean hasInvalidService = directorServices.stream()
			.anyMatch(service -> service.getParent() == null);

		if (hasInvalidService) {
			throw new CustomRuntimeException(DirectorServiceException.INVALID_SERVICE);
		}
	}

	/**
	 * 디렉터가 해당 서비스를 소유하는지 확인한다.
	 *
	 * @param directorInfo
	 * @param directorServiceId
	 * @return
	 */
	public DirectorServiceMapping validateServiceOwnership(DirectorInfo directorInfo, Long directorServiceId) {
		return directorInfo.getDirectorServiceMappings().stream()
			.filter(mapping -> mapping.getDirectorService().getId().equals(directorServiceId))
			.findFirst()
			.orElseThrow(() -> new CustomRuntimeException(DirectorServiceException.DIRECTOR_SERVICE_NOT_FOUND));
	}
}
