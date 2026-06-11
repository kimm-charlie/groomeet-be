package com.motd.be.module.admin.banner.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.banner.entity.Banner;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BannerAdminFindAllResponseForAdmin {

	private Integer totalCount;
	private Integer page;
	private Boolean hasNext;
	private List<BannerAdminResponseForAdmin> banners;

	public static BannerAdminFindAllResponseForAdmin from(Slice<Banner> banners) {
		return BannerAdminFindAllResponseForAdmin.builder()
			.page(banners.getNumber())
			.hasNext(banners.hasNext())
			.banners(BannerAdminResponseForAdmin.fromList(banners.getContent()))
			.totalCount(banners.getNumberOfElements())
			.build();
	}
}
