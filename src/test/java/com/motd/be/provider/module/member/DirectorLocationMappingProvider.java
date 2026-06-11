package com.motd.be.provider.module.member;

import static com.motd.be.common.utils.Utils.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_location_mapping.entity.DirectorLocationMapping;
import com.motd.be.module.member.director_location_mapping.repository.DirectorLocationMappingRepository;
import com.motd.be.module.member.location.entity.Location;

@Component
public class DirectorLocationMappingProvider {

	@Autowired
	private DirectorLocationMappingRepository directorLocationMappingRepository;

	public DirectorLocationMapping save(DirectorInfo directorInfo, Location location) {
		return directorLocationMappingRepository.save(DirectorLocationMapping.builder()
			.directorInfo(directorInfo)
			.location(location)
			.activeUniqueKey(generateDirectorInfoLocationMappingUniqueKey(directorInfo, location))
			.build());
	}

	public List<DirectorLocationMapping> findAll() {
		return directorLocationMappingRepository.findAll();
	}
}
