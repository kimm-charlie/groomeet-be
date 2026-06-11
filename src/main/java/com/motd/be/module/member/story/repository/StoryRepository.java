package com.motd.be.module.member.story.repository;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.motd.be.module.member.story.entity.Story;

public interface StoryRepository extends JpaRepository<Story, Long> {

	@Query("""
			SELECT s
			FROM Story s
			WHERE s.isDeleted = false
			ORDER BY s.sortOrder ASC
		""")
	Slice<Story> findAllWithPageable(Pageable pageable);

	@Query("""
			SELECT s
			FROM Story s
			WHERE s.id = :storyId
			AND s.isDeleted = false
		""")
	Optional<Story> findByIdAndIsDeletedFalse(Long storyId);
}
