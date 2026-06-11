package com.motd.be.module.director.banner.dto.response;

import java.util.List;

import com.motd.be.module.member.banner.entity.Banner;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BannerFindAllResponseForDirector {

	private List<BannerResponseForDirector> events;
	private Integer totalCount;

	public static BannerFindAllResponseForDirector from(List<Banner> banners) {
		return BannerFindAllResponseForDirector.builder()
			.events(banners.stream().map(BannerResponseForDirector::from).toList())
			.totalCount(banners.size())
			.build();
	}
}
