package com.motd.be.module.admin.banner.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.admin.banner.repository.BannerRepositoryForAdmin;
import com.motd.be.module.member.banner.entity.Banner;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BannerCommandServiceForAdmin {

	private final BannerRepositoryForAdmin bannerRepositoryForAdmin;

	public Banner save(Banner banner) {
		return bannerRepositoryForAdmin.save(banner);
	}

	public void incrementSortOrder(Long id, int sortOrder) {
		bannerRepositoryForAdmin.incrementSortOrder(id, sortOrder);
	}

	public void incrementSortOrderWithStartAndEnd(int start, int end) {
		bannerRepositoryForAdmin.incrementSortOrderWithStartAndEnd(start, end);
	}

	public void decrementSortOrderWithStartAndEnd(int start, int end) {
		bannerRepositoryForAdmin.decrementSortOrderWithStartAndEnd(start, end);
	}

	public void decrementSortOrder(int sortOrder) {
		bannerRepositoryForAdmin.decrementSortOrder(sortOrder);
	}
}
