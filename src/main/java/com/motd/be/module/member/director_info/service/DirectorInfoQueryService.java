package com.motd.be.module.member.director_info.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_info.repository.DirectorInfoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectorInfoQueryService {

	private final DirectorInfoRepository directorInfoRepository;

	public Slice<DirectorInfo> findDirectorRank(Pageable pageable) {
		return directorInfoRepository.findDirectorRank(pageable);
	}
}
