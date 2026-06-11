package com.motd.be.module.admin.banner.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import com.motd.be.module.member.banner.entity.Banner;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BannerAdminResponseForAdmin {

	private Long id;
	private String title;
	private String thumbnailImageUrl;
	private String contentImageUrl;
	private Long thumbnailFileId;
	private Long contentFileId;
	private String createdAt;
	private String startAt;
	private String endAt;
	private Integer sortOrder;
	private Boolean isDeleted;
	private Boolean isWebViewBanner;
	private String webViewUrl;
	private String type;

	public static List<BannerAdminResponseForAdmin> fromList(List<Banner> banners) {
		return banners.stream()
			.map(BannerAdminResponseForAdmin::from)
			.collect(Collectors.toList());
	}

	public static BannerAdminResponseForAdmin from(Banner banner) {
		return BannerAdminResponseForAdmin.builder()
			.id(banner.getId())
			.title(banner.getTitle())
			.thumbnailImageUrl(banner.getThumbnailImageCdnUrl())
			.contentImageUrl(banner.getContentImageCdnUrl())
			.thumbnailFileId(banner.getThumbnailFile() != null ? banner.getThumbnailFile().getId() : null)
			.contentFileId(banner.getContentFile() != null ? banner.getContentFile().getId() : null)
			.createdAt(banner.getFormattedCreatedAt())
			.startAt(banner.getFormattedStartAt())
			.endAt(banner.getFormattedEndAt())
			.sortOrder(banner.getSortOrder())
			.isDeleted(banner.getIsDeleted())
			.isWebViewBanner(banner.getIsWebViewBanner())
			.webViewUrl(banner.getWebViewUrl())
			.type(banner.getType().name())
			.build();
	}
}
