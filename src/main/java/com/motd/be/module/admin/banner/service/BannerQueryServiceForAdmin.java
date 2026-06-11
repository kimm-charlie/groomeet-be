package com.motd.be.module.admin.banner.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.BannerException;
import com.motd.be.module.admin.banner.repository.BannerQueryDslRepositoryForAdmin;
import com.motd.be.module.admin.banner.repository.BannerRepositoryForAdmin;
import com.motd.be.module.member.banner.entity.Banner;
import com.motd.be.module.member.banner.entity.BannerType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerQueryServiceForAdmin {

	private final BannerRepositoryForAdmin bannerRepositoryForAdmin;
	private final BannerQueryDslRepositoryForAdmin bannerQueryDslRepositoryForAdmin;

	public Banner findById(Long bannerId) {
		return bannerRepositoryForAdmin.findByIdAndIsDeletedFalse(bannerId)
			.orElseThrow(() -> new CustomRuntimeException(BannerException.NOT_FOUND));
	}

	public Banner findByIdIncludingDeleted(Long bannerId) {
		return bannerRepositoryForAdmin.findById(bannerId)
			.orElseThrow(() -> new CustomRuntimeException(BannerException.NOT_FOUND));
	}

	public Slice<Banner> findAll(Pageable pageable, Boolean showIsDeleted, BannerType type) {
		return bannerQueryDslRepositoryForAdmin.findAll(pageable, showIsDeleted, type);
	}

	public Optional<Banner> findActiveMemberBannerByTitleContaining(String keyword) {
		return bannerRepositoryForAdmin.findAllActiveByTitleContaining(LocalDateTime.now(), BannerType.MEMBER, keyword)
			.stream()
			.findFirst();
	}
}
