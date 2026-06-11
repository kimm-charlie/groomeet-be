package com.motd.be.module.admin.banner_file.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.admin.admin_file.entity.AdminFileForAdmin;
import com.motd.be.module.admin.banner_file.repository.BannerFileRepositoryForAdmin;
import com.motd.be.module.member.banner_file.entity.BannerFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BannerFileCommandServiceForAdmin {

	private final BannerFileRepositoryForAdmin bannerFileRepositoryForAdmin;

	public AdminFileForAdmin save(BannerFile bannerFile) {
		return bannerFileRepositoryForAdmin.save(bannerFile);
	}
}
