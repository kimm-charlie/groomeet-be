package com.motd.be.module.member.location.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;

public interface LocationRepository extends JpaRepository<Location, Long> {

	@Query("""
			SELECT L
			FROM Location L
			WHERE L.id = :id
		""")
	Optional<Location> findById(Long id);

	@Query("""
			SELECT L
			FROM Location L
			WHERE (:locationParentId IS NULL AND L.parent IS NULL)
					OR (:locationParentId IS NOT NULL AND L.parent.id = :locationParentId)
			ORDER BY L.id asc
		""")
	List<Location> findAllByParentId(Long locationParentId);

	@Query("""
			SELECT L
			FROM Location L
			WHERE L.type = :locationType
		""")
	Optional<Location> findAllCityType(LocationType locationType);
}
