package com.motd.be.module.director.popup.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.popup.entity.Popup;
import com.motd.be.module.member.popup.entity.PopupType;

public interface PopupRepositoryForDirector extends JpaRepository<Popup, Long> {

	@Query("""
			SELECT p FROM Popup p
			WHERE p.isDeleted = false
			AND p.startAt <= :now
			AND p.endAt >= :now
			AND p.type = :type
			ORDER BY p.sortOrder ASC
		""")
	List<Popup> findAllActive(@Param("now") LocalDateTime now, @Param("type") PopupType type);
}
