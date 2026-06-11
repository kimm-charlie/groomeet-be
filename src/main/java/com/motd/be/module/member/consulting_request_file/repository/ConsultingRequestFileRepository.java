package com.motd.be.module.member.consulting_request_file.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_request_file.entity.ConsultingRequestFile;
import com.motd.be.module.member.consulting_request_file.enums.ConsultingRequestImageCategory;
import com.motd.be.module.member.member.entity.Member;

public interface ConsultingRequestFileRepository extends JpaRepository<ConsultingRequestFile, Long> {

	@Query("""
			SELECT crf
			FROM ConsultingRequestFile crf
			WHERE crf.id IN :ids
			AND crf.isDeleted = false
		""")
	List<ConsultingRequestFile> findAllByIds(@Param("ids") List<Long> ids);

	@Query("""
			SELECT crf
			FROM ConsultingRequestFile crf
			WHERE crf.id IN :ids
			AND crf.member = :member
			AND crf.consultingRequest IS NULL
			AND crf.isDeleted = false
		""")
	List<ConsultingRequestFile> findAllByIdsAndMember(@Param("ids") List<Long> ids, @Param("member") Member member);

	@Modifying
	@Query("""
			UPDATE ConsultingRequestFile crf
			SET crf.consultingRequest = :consultingRequest,
			    crf.imageCategory = :imageCategory,
			    crf.sortOrder = :sortOrder
			WHERE crf.id = :fileId
			  AND crf.member = :member
			  AND crf.consultingRequest IS NULL
			  AND crf.isDeleted = false
		""")
	int updateConsultingRequestMapping(
		@Param("fileId") Long fileId,
		@Param("member") Member member,
		@Param("consultingRequest") ConsultingRequest consultingRequest,
		@Param("imageCategory") ConsultingRequestImageCategory imageCategory,
		@Param("sortOrder") int sortOrder);

	@Query("""
		        SELECT crf
		        FROM ConsultingRequestFile crf
		        WHERE crf.fileKey = :fileKey
		        AND crf.isDeleted = false
		""")
	Optional<ConsultingRequestFile> findByFileKey(String fileKey);
}
