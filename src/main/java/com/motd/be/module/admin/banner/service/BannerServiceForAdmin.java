package com.motd.be.module.admin.banner.service;

import static com.motd.be.common.constants.PageSizeConstants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.module.admin.banner.dto.request.BannerSaveRequestForAdmin;
import com.motd.be.module.admin.banner.dto.request.BannerUpdateRequestForAdmin;
import com.motd.be.module.admin.banner.dto.response.BannerAdminFindAllResponseForAdmin;
import com.motd.be.module.admin.banner.dto.response.BannerAdminResponseForAdmin;
import com.motd.be.module.member.banner.entity.Banner;
import com.motd.be.module.member.banner.entity.BannerType;
import com.motd.be.module.member.banner.validator.BannerValidator;
import com.motd.be.module.member.banner_file.entity.BannerFile;
import com.motd.be.shared.aws.enums.S3DirectoryType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BannerServiceForAdmin {

	private final BannerCommandServiceForAdmin bannerCommandServiceForAdmin;
	private final BannerQueryServiceForAdmin bannerQueryServiceForAdmin;
	private final BannerValidator bannerValidator;

	public Banner save(BannerSaveRequestForAdmin request, Map<S3DirectoryType, BannerFile> bannerFileMap) {
		BannerFile thumbnailFile =
			request.getType().equals(BannerType.MEMBER)
				? bannerFileMap.get(S3DirectoryType.MEMBER_BANNER_THUMBNAIL)
				: bannerFileMap.get(S3DirectoryType.DIRECTOR_BANNER_THUMBNAIL);
		BannerFile contentFile = request.getType().equals(BannerType.MEMBER)
			? bannerFileMap.get(S3DirectoryType.MEMBER_BANNER_CONTENT)
			: bannerFileMap.get(S3DirectoryType.DIRECTOR_BANNER_CONTENT);

		// 날짜 검증
		bannerValidator.validateBannerDate(request.getStartAt(), request.getEndAt());

		// 웹뷰 배너 URL 검증
		bannerValidator.validateContentBannerUrl(request.getIsWebViewBanner(), contentFile);

		// 썸네일 이미지 존재여부 검증
		bannerValidator.validateThumbnailFileExists(thumbnailFile);

		Banner savedBanner = bannerCommandServiceForAdmin.save(request.toEntity(thumbnailFile, contentFile));

		bannerCommandServiceForAdmin.incrementSortOrder(savedBanner.getId(), request.getSortOrder());

		return savedBanner;
	}

	public Banner update(Long bannerId, BannerUpdateRequestForAdmin request,
		Map<S3DirectoryType, BannerFile> bannerFileMap) {
		Banner banner = bannerQueryServiceForAdmin.findById(bannerId);

		BannerFile thumbnailFile =
			banner.getType().equals(BannerType.MEMBER)
				? bannerFileMap.get(S3DirectoryType.MEMBER_BANNER_THUMBNAIL)
				: bannerFileMap.get(S3DirectoryType.DIRECTOR_BANNER_THUMBNAIL);
		BannerFile contentFile = banner.getType().equals(BannerType.MEMBER)
			? bannerFileMap.get(S3DirectoryType.MEMBER_BANNER_CONTENT)
			: bannerFileMap.get(S3DirectoryType.DIRECTOR_BANNER_CONTENT);

		// 날짜 검증
		bannerValidator.validateBannerDate(request.getStartAt(), request.getEndAt());

		// 웹뷰 배너 URL 검증
		bannerValidator.validateContentBannerUrl(request.getIsWebViewBanner(), contentFile);

		// 썸네일 이미지 존재여부 검증
		bannerValidator.validateThumbnailFileExists(thumbnailFile);

		// 기존 배너 이미지 필요시 삭제 처리
		banner.getThumbnailFile().deleteIfNeeded(thumbnailFile);

		if (banner.getContentFile() != null) {
			banner.getContentFile().deleteIfNeeded(contentFile);
		}

		// 배너 정보 수정
		banner.updateInfo(request.getTitle(), parseToLocalDateTime(request.getStartAt()),
			parseToLocalDateTime(request.getEndAt()), request.getIsWebViewBanner(), request.getWebViewUrl(),
			thumbnailFile, contentFile);

		// 정렬순서 변경
		if (!banner.getSortOrder().equals(request.getSortOrder())) {
			updateOrder(banner, request.getSortOrder());
		}

		return banner;
	}

	public void delete(Banner banner) {
		banner.delete();
		bannerCommandServiceForAdmin.decrementSortOrder(banner.getSortOrder());
	}

	public BannerAdminFindAllResponseForAdmin findAll(int page, Boolean showIsDeleted, BannerType type) {
		Pageable pageable = PageRequest.of(page, BANNER_PAGE_SIZE);

		Slice<Banner> banners = bannerQueryServiceForAdmin.findAll(pageable, showIsDeleted, type);

		return BannerAdminFindAllResponseForAdmin.from(banners);
	}

	public BannerAdminResponseForAdmin findDetail(Long bannerId) {
		return BannerAdminResponseForAdmin.from(bannerQueryServiceForAdmin.findByIdIncludingDeleted(bannerId));
	}

	private void updateOrder(Banner banner, int newOrder) {
		int originalOrder = banner.getSortOrder();

		if (newOrder < originalOrder) {
			bannerCommandServiceForAdmin.incrementSortOrderWithStartAndEnd(newOrder, originalOrder - 1);
		} else if (newOrder > originalOrder) {
			bannerCommandServiceForAdmin.decrementSortOrderWithStartAndEnd(originalOrder + 1, newOrder);
		}

		banner.updateSortOrder(newOrder);
	}

}
