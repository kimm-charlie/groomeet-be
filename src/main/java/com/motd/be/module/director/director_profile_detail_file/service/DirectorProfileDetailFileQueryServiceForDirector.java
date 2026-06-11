package com.motd.be.module.director.director_profile_detail_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.director_profile_detail_file.entity.DirectorProfileDetailFile;
import com.motd.be.module.member.director_profile_detail_file.repository.DirectorProfileDetailFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectorProfileDetailFileQueryServiceForDirector {

	private final DirectorProfileDetailFileRepository directorProfileDetailFileRepository;

	public List<DirectorProfileDetailFile> findAllByIds(List<Long> fileIds) {
		return directorProfileDetailFileRepository.findAllByIds(fileIds);
	}
}
