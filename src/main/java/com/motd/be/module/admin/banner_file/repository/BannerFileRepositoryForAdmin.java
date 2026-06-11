package com.motd.be.module.admin.banner_file.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.motd.be.module.member.banner_file.entity.BannerFile;

public interface BannerFileRepositoryForAdmin extends JpaRepository<BannerFile, Long> {

	@Query("""
			SELECT b
			FROM BannerFile b
			WHERE b.id IN :fileIds
			AND b.isDeleted = false
		""")
	List<BannerFile> findAllByIds(List<Long> fileIds);

	@Query("""
			SELECT b
			FROM  BannerFile b
			WHERE b.fileKey = :fileKey
			AND b.isDeleted = false
		""")
	Optional<BannerFile> findByFileKey(String fileKey);
}
