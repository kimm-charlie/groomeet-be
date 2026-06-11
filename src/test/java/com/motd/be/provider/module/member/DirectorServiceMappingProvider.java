package com.motd.be.provider.module.member;

import static com.motd.be.common.utils.Utils.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;
import com.motd.be.module.member.director_service_mapping.repository.DirectorServiceMappingRepository;

@Component
public class DirectorServiceMappingProvider {

	@Autowired
	private DirectorServiceMappingRepository directorInfoDirectorServiceMappingRepository;

	public DirectorServiceMapping save(DirectorInfo directorInfo, DirectorService directorService) {
		return directorInfoDirectorServiceMappingRepository.save(DirectorServiceMapping.builder()
			.directorService(directorService)
			.directorInfo(directorInfo)
			.activeUniqueKey(generateDirectorInfoDirectorServiceMappingUniqueKey(directorInfo, directorService))
			.build());
	}

	public DirectorServiceMapping findById(Long id) {
		return directorInfoDirectorServiceMappingRepository.findById(id).orElse(null);
	}

	public List<DirectorServiceMapping> findAll() {
		return directorInfoDirectorServiceMappingRepository.findAll();
	}

	public void saveWithIsDeletedTrue(DirectorInfo directorInfo, DirectorService service) {
		directorInfoDirectorServiceMappingRepository.save(DirectorServiceMapping.builder()
			.directorInfo(directorInfo)
			.directorService(service)
			.isDeleted(true)
			.activeUniqueKey(generateDirectorInfoDirectorServiceMappingUniqueKey(directorInfo, service))
			.build());
	}
}
