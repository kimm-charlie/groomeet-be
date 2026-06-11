package com.motd.be.module.director.banner.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.banner.repository.BannerRepositoryForDirector;
import com.motd.be.module.member.banner.entity.Banner;
import com.motd.be.module.member.banner.entity.BannerType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerQueryServiceForDirector {

	private final BannerRepositoryForDirector bannerRepositoryForDirector;

	public List<Banner> findAllActive() {
		return bannerRepositoryForDirector.findAllActive(LocalDateTime.now(), BannerType.DIRECTOR);
	}
}
