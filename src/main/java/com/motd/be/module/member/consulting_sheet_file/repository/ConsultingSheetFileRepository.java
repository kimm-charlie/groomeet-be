package com.motd.be.module.member.consulting_sheet_file.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.consulting_sheet_file.entity.ConsultingSheetFile;

public interface ConsultingSheetFileRepository extends JpaRepository<ConsultingSheetFile, Long> {

	@Query("""
			SELECT csf
			FROM ConsultingSheetFile csf
			WHERE csf.id IN :ids
			AND csf.isDeleted = false
		""")
	List<ConsultingSheetFile> findAllByIds(@Param("ids") List<Long> ids);

	@Modifying
	@Query("""
		        UPDATE ConsultingSheetFile csf
		        SET csf.consultingSheet = :consultingSheet
		        WHERE csf IN :consultingSheetFiles
		          AND csf.isDeleted = false
		""")
	void mapConsultingSheet(@Param("consultingSheetFiles") List<ConsultingSheetFile> consultingSheetFiles,
		@Param("consultingSheet") ConsultingSheet consultingSheet);

	@Query("""
		        SELECT csf
		        FROM ConsultingSheetFile csf
		        WHERE csf.fileKey = :fileKey
		        AND csf.isDeleted = false
		""")
	Optional<ConsultingSheetFile> findByFileKey(String fileKey);
}
