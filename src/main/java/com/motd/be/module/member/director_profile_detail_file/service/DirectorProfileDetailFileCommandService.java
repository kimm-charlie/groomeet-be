package com.motd.be.module.member.director_profile_detail_file.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.director_profile_detail_file.entity.DirectorProfileDetailFile;
import com.motd.be.module.member.director_profile_detail_file.repository.DirectorProfileDetailFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectorProfileDetailFileCommandService {

	private final DirectorProfileDetailFileRepository directorProfileDetailFileRepository;

	public DirectorProfileDetailFile save(DirectorProfileDetailFile entity) {
		return directorProfileDetailFileRepository.save(entity);
	}
}
