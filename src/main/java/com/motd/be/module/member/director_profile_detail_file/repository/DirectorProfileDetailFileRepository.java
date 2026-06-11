package com.motd.be.module.member.director_profile_detail_file.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.motd.be.module.member.director_profile_detail.entity.DirectorProfileDetail;
import com.motd.be.module.member.director_profile_detail_file.entity.DirectorProfileDetailFile;

public interface DirectorProfileDetailFileRepository extends JpaRepository<DirectorProfileDetailFile, Long> {

	@Query("""
			SELECT dpdf
			FROM DirectorProfileDetailFile dpdf
			WHERE dpdf.id IN :ids
			AND dpdf.isDeleted = false
		""")
	List<DirectorProfileDetailFile> findAllByIds(List<Long> ids);

	@Modifying
	@Query("""
			UPDATE DirectorProfileDetailFile dpdf
			SET dpdf.isDeleted = true
			WHERE dpdf.id IN :deleteIds
		""")
	void deleteAllByIds(Set<Long> deleteIds);

	@Modifying
	@Query("""
		        UPDATE DirectorProfileDetailFile dpdf
		        SET dpdf.directorProfileDetail = :directorProfileDetail
		        WHERE dpdf.id IN :addIds
		""")
	void mapFilesToDirectorProfileDetail(DirectorProfileDetail directorProfileDetail, Set<Long> addIds);

	@Query("""
		        SELECT dpdf
		        FROM DirectorProfileDetailFile dpdf
		        WHERE dpdf.fileKey = :fileKey
		        AND dpdf.isDeleted = false
		""")
	Optional<DirectorProfileDetailFile> findByFileKey(String fileKey);
}
