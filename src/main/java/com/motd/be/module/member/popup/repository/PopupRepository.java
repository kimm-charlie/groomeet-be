package com.motd.be.module.member.popup.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.popup.entity.Popup;

@Repository
public interface PopupRepository extends JpaRepository<Popup, Long> {

	@Query("""	
			SELECT p FROM Popup p
			WHERE p.isDeleted = false
			AND :now BETWEEN p.startAt AND p.endAt
			ORDER BY p.sortOrder ASC
		""")
	List<Popup> findAll(LocalDateTime now);
}
