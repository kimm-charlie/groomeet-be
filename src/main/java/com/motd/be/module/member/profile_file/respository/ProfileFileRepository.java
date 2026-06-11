package com.motd.be.module.member.profile_file.respository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.profile_file.entity.ProfileFile;

public interface ProfileFileRepository extends JpaRepository<ProfileFile, Long> {

	@Query("""
			SELECT p
			FROM ProfileFile p
			WHERE p.id = :id
			AND p.isDeleted = false
		""")
	Optional<ProfileFile> findByIdWithIsDeletedFalse(Long id);

	@Modifying
	@Query("""
			UPDATE ProfileFile p
			SET p.isDeleted = true
			WHERE p.member = :member
			AND p.isDeleted = false
			AND p.id != :excludingProfileFileId
		""")
	void deleteByMemberAndIdNot(Member member, Long excludingProfileFileId);

	@Modifying
	@Query("""
			UPDATE ProfileFile p
			SET p.isDeleted = true
			WHERE p.member = :member
			AND p.isDeleted = false
		""")
	void deleteByMember(Member member);

	@Query("""
			SELECT p
			FROM ProfileFile p
			WHERE p.id IN :ids
		""")
	List<ProfileFile> findAllByIds(List<Long> ids);

	@Query("""
		        SELECT p
		        FROM ProfileFile p
		        WHERE p.fileKey = :fileKey
		        AND p.isDeleted = false
		""")
	Optional<ProfileFile> findByFileKey(String fileKey);
}
