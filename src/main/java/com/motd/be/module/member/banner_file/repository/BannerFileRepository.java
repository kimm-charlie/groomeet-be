package com.motd.be.module.member.banner_file.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motd.be.module.member.banner_file.entity.BannerFile;

public interface BannerFileRepository extends JpaRepository<BannerFile, Long> {
}
