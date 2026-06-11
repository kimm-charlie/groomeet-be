package com.motd.be.module.member.report_file.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.report.entity.Report;
import com.motd.be.module.member.report_file.entity.ReportFile;

public interface ReportFileRepository extends JpaRepository<ReportFile, Long> {

	@Query("""
			 SELECT rf
			 FROM ReportFile rf
			 WHERE rf.id IN :ids
			 AND rf.isDeleted = false
		""")
	List<ReportFile> findAllByIds(List<Long> ids);

	@Modifying
	@Query("""
		        UPDATE ReportFile rf
		        SET rf.report = :report
		        WHERE rf IN :files
		""")
	void mapFilesToReport(@Param("report") Report report, @Param("files") List<ReportFile> files);

	@Query("""
		        SELECT rf
		        FROM ReportFile rf
		        WHERE rf.fileKey = :fileKey
		        AND rf.isDeleted = false
		""")
	Optional<ReportFile> findByFileKey(String fileKey);
}
