package com.motd.be.module.director.banner.facade;

import org.springframework.stereotype.Component;

import com.motd.be.module.director.banner.dto.response.BannerFindAllResponseForDirector;
import com.motd.be.module.director.banner.service.BannerQueryServiceForDirector;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BannerFacadeForDirector {

	private final BannerQueryServiceForDirector bannerQueryServiceForDirector;

	public BannerFindAllResponseForDirector findAll() {
		return BannerFindAllResponseForDirector.from(bannerQueryServiceForDirector.findAllActive());
	}
}
