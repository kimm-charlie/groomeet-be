package com.motd.be.module.admin.popup.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.popup.entity.Popup;

public interface PopupAdminRepository extends JpaRepository<Popup, Long> {

	@Modifying
	@Query(""" 
			UPDATE Popup p 
			SET p.sortOrder = p.sortOrder + 1 
			WHERE p.sortOrder >= :sortOrder
			AND p.id != :id 
			AND p.isDeleted = false
			AND :now BETWEEN p.startAt AND p.endAt
		""")
	void incrementSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder,
		@Param("now") LocalDateTime now);

	@Modifying
	@Query("""
		    UPDATE Popup p
		    SET p.sortOrder = p.sortOrder + 1
		    WHERE p.sortOrder BETWEEN :start AND :end
		    AND p.isDeleted = false
			AND :now BETWEEN p.startAt AND p.endAt
		""")
	void incrementSortOrderWithStartAndEnd(@Param("start") int start, @Param("end") int end,
		@Param("now") LocalDateTime now);

	@Modifying
	@Query("""
		    UPDATE Popup p
		    SET p.sortOrder = p.sortOrder - 1
		    WHERE p.sortOrder BETWEEN :start AND :end
		    AND p.isDeleted = false
			AND :now BETWEEN p.startAt AND p.endAt
		""")
	void decrementSortOrderWithStartAndEnd(@Param("start") int start, @Param("end") int end,
		@Param("now") LocalDateTime now);

	@Modifying
	@Query("""
			UPDATE Popup p
			SET p.sortOrder = p.sortOrder - 1
			WHERE p.sortOrder > :sortOrder
			AND p.isDeleted = false
			AND :now BETWEEN p.startAt AND p.endAt
		""")
	void decrementSortOrder(@Param("sortOrder") int sortOrder, @Param("now") LocalDateTime now);
}
