package com.motd.be.module.director.director_profile_detail_file.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motd.be.module.member.director_profile_detail_file.entity.DirectorProfileDetailFile;

public interface DirectorProfileDetailFileRepositoryForDirector extends JpaRepository<DirectorProfileDetailFile, Long> {
}
