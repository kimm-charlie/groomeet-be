package com.motd.be.module.director.director_profile_detail_file.service;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.director_profile_detail.entity.DirectorProfileDetail;
import com.motd.be.module.member.director_profile_detail_file.repository.DirectorProfileDetailFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectorProfileDetailFileCommandServiceForDirector {

	private final DirectorProfileDetailFileRepository directorProfileDetailFileRepository;

	public void deleteAllByIds(Set<Long> deleteIds) {
		directorProfileDetailFileRepository.deleteAllByIds(deleteIds);
	}

	public void mapFilesToDirectorProfileDetail(DirectorProfileDetail directorProfileDetail, Set<Long> addIds) {
		directorProfileDetailFileRepository.mapFilesToDirectorProfileDetail(directorProfileDetail, addIds);
	}
}
