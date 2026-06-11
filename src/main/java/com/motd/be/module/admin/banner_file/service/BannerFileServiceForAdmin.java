package com.motd.be.module.admin.banner_file.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.motd.be.module.admin.admin_file.entity.AdminFileForAdmin;
import com.motd.be.module.member.banner.entity.Banner;
import com.motd.be.module.member.banner_file.entity.BannerFile;
import com.motd.be.shared.aws.enums.S3DirectoryType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BannerFileServiceForAdmin {

	private final BannerFileQueryServiceForAdmin bannerFileQueryServiceForAdmin;

	public Map<S3DirectoryType, BannerFile> getBannerFiles(Long thumbnailFileId, Long contentFileId) {
		List<BannerFile> bannerFiles = bannerFileQueryServiceForAdmin.findAllByIds(
			Arrays.asList(thumbnailFileId, contentFileId));

		return bannerFiles.stream()
			.collect(
				java.util.stream.Collectors.toMap(
					AdminFileForAdmin::getDirectoryType,
					bannerFile -> bannerFile
				)
			);
	}

	public void deleteBannerFiles(Banner banner) {
		// 파일도 soft delete
		if (banner.getThumbnailFile() != null) {
			banner.getThumbnailFile().delete();
		}
		if (banner.getContentFile() != null) {
			banner.getContentFile().delete();
		}
	}
}
