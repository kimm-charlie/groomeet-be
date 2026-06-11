package com.motd.be.module.member.banner.dto.response;

import com.motd.be.module.member.banner.entity.Banner;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BannerResponse {

	private Long id;
	private String title;
	private String contentImageCdnUrl;
	private String thumbnailImageCdnUrl;
	private Boolean isWebViewBanner;
	private String webViewUrl;

	public static BannerResponse from(Banner banner) {
		return BannerResponse.builder()
			.id(banner.getId())
			.title(banner.getTitle())
			.contentImageCdnUrl(banner.getContentImageCdnUrl())
			.thumbnailImageCdnUrl(banner.getThumbnailImageCdnUrl())
			.isWebViewBanner(banner.getIsWebViewBanner())
			.webViewUrl(banner.getWebViewUrl())
			.build();
	}
}
