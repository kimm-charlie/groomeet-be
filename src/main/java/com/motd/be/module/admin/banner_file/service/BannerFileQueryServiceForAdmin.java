package com.motd.be.module.admin.banner_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.BannerFileException;
import com.motd.be.module.admin.admin_file.entity.AdminFileForAdmin;
import com.motd.be.module.admin.banner_file.repository.BannerFileRepositoryForAdmin;
import com.motd.be.module.member.banner_file.entity.BannerFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BannerFileQueryServiceForAdmin {

	private final BannerFileRepositoryForAdmin bannerFileRepositoryForAdmin;

	public List<? extends AdminFileForAdmin> findAllByIdsAdminFileType(List<Long> fileIds) {
		return bannerFileRepositoryForAdmin.findAllByIds(fileIds);
	}

	public AdminFileForAdmin findByFileKey(String fileKey) {
		return bannerFileRepositoryForAdmin.findByFileKey(fileKey).orElseThrow(() -> new CustomRuntimeException(
			BannerFileException.NOT_FOUND));
	}

	public List<BannerFile> findAllByIds(List<Long> fileIds) {
		return bannerFileRepositoryForAdmin.findAllByIds(fileIds);
	}

	public BannerFile findById(Long fileId) {
		return bannerFileRepositoryForAdmin.findById(fileId)
			.orElseThrow(() -> new CustomRuntimeException(BannerFileException.NOT_FOUND));
	}
}
