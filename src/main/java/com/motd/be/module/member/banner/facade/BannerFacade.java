package com.motd.be.module.member.banner.facade;

import org.springframework.stereotype.Component;

import com.motd.be.module.member.banner.dto.response.BannerFindAllResponse;
import com.motd.be.module.member.banner.entity.BannerType;
import com.motd.be.module.member.banner.service.BannerQueryService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BannerFacade {

	private final BannerQueryService bannerQueryService;

	public BannerFindAllResponse findAll() {
		return BannerFindAllResponse.from(bannerQueryService.findAllActive(BannerType.MEMBER));
	}
}
