package com.motd.be.module.admin.banner.facade;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.admin.banner.dto.request.BannerSaveRequestForAdmin;
import com.motd.be.module.admin.banner.dto.request.BannerUpdateRequestForAdmin;
import com.motd.be.module.admin.banner.dto.response.BannerAdminFindAllResponseForAdmin;
import com.motd.be.module.admin.banner.dto.response.BannerAdminResponseForAdmin;
import com.motd.be.module.admin.banner.service.BannerQueryServiceForAdmin;
import com.motd.be.module.admin.banner.service.BannerServiceForAdmin;
import com.motd.be.module.admin.banner_file.service.BannerFileServiceForAdmin;
import com.motd.be.module.member.banner.entity.Banner;
import com.motd.be.module.member.banner.entity.BannerType;
import com.motd.be.module.member.banner_file.entity.BannerFile;
import com.motd.be.shared.aws.enums.S3DirectoryType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerFacadeForAdmin {

	private final BannerServiceForAdmin bannerServiceForAdmin;
	private final BannerFileServiceForAdmin bannerFileServiceForAdmin;
	private final BannerQueryServiceForAdmin bannerQueryServiceForAdmin;

	@Transactional
	public void save(BannerSaveRequestForAdmin request) {
		Map<S3DirectoryType, BannerFile> bannerFileMap = bannerFileServiceForAdmin.getBannerFiles(
			request.getThumbnailFileId(), request.getContentFileId());

		bannerServiceForAdmin.save(request, bannerFileMap);
	}

	@Transactional
	public void update(Long bannerId, BannerUpdateRequestForAdmin request) {
		Map<S3DirectoryType, BannerFile> bannerFileMap = bannerFileServiceForAdmin.getBannerFiles(
			request.getThumbnailFileId(), request.getContentFileId());

		bannerServiceForAdmin.update(bannerId, request, bannerFileMap);
	}

	@Transactional
	public void delete(Long bannerId) {
		Banner banner = bannerQueryServiceForAdmin.findById(bannerId);

		bannerServiceForAdmin.delete(banner);

		// 파일 삭제
		bannerFileServiceForAdmin.deleteBannerFiles(banner);
	}

	public BannerAdminFindAllResponseForAdmin findAll(int page, Boolean showIsDeleted, BannerType type) {
		return bannerServiceForAdmin.findAll(page, showIsDeleted, type);
	}

	public BannerAdminResponseForAdmin findDetail(Long bannerId) {
		return bannerServiceForAdmin.findDetail(bannerId);
	}
}
