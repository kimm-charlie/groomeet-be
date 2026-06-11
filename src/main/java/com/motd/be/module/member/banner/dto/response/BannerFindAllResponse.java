package com.motd.be.module.member.banner.dto.response;

import java.util.List;

import com.motd.be.module.member.banner.entity.Banner;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BannerFindAllResponse {

	private List<BannerResponse> events;
	private Integer totalCount;

	public static BannerFindAllResponse from(List<Banner> banners) {
		return BannerFindAllResponse.builder()
			.events(banners.stream().map(BannerResponse::from).toList())
			.totalCount(banners.size())
			.build();
	}
}
