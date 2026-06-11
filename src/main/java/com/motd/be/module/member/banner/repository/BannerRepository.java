package com.motd.be.module.member.banner.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.banner.entity.Banner;
import com.motd.be.module.member.banner.entity.BannerType;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

	@Query("""
			SELECT b FROM Banner b
			WHERE b.isDeleted = false
			AND b.startAt <= :now
			AND b.endAt >= :now
			AND b.type = :type
			ORDER BY b.sortOrder ASC
		""")
	List<Banner> findAllActive(@Param("now") LocalDateTime now, @Param("type") BannerType type);
}
