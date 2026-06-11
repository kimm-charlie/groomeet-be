package com.motd.be.module.director.location.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;

public interface LocationRepositoryForDirector extends JpaRepository<Location, Long> {

	@Query("""
			SELECT L
			FROM Location L
			WHERE L.type = :locationType
		""")
	Optional<Location> findAllCityType(LocationType locationType);
}
