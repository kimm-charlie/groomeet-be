package com.motd.be.module.member.banner.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.banner.entity.Banner;
import com.motd.be.module.member.banner.entity.BannerType;
import com.motd.be.module.member.banner.repository.BannerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerQueryService {

	private final BannerRepository bannerRepository;

	public List<Banner> findAllActive(BannerType type) {
		return bannerRepository.findAllActive(LocalDateTime.now(), type);
	}

	public Optional<Banner> findById(Long id) {
		return bannerRepository.findById(id);
	}
}
