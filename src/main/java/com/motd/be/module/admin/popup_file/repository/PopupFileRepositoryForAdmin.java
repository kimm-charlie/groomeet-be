package com.motd.be.module.admin.popup_file.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.motd.be.module.admin.admin_file.entity.AdminFileForAdmin;
import com.motd.be.module.member.popup_file.entity.PopupFile;

public interface PopupFileRepositoryForAdmin extends JpaRepository<PopupFile, Long> {

	@Query("""
			SELECT p
			FROM PopupFile p
			WHERE p.id IN :fileIds
			AND p.isDeleted = false
		""")
	List<PopupFile> findAllByIds(List<Long> fileIds);

	@Query("""
			SELECT p
			FROM PopupFile p
			WHERE p.fileKey = :fileKey
			AND p.isDeleted = false
		""")
	Optional<AdminFileForAdmin> findByFileKey(String fileKey);
}
