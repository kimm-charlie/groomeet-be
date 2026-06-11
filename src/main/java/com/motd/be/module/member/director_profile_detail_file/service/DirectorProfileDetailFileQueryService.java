package com.motd.be.module.member.director_profile_detail_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.FileException;
import com.motd.be.module.member.director_profile_detail_file.entity.DirectorProfileDetailFile;
import com.motd.be.module.member.director_profile_detail_file.repository.DirectorProfileDetailFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectorProfileDetailFileQueryService {

	private final DirectorProfileDetailFileRepository directorProfileDetailFileRepository;

	public List<DirectorProfileDetailFile> findAllByIds(List<Long> ids) {
		return directorProfileDetailFileRepository.findAllByIds(ids);
	}

	public DirectorProfileDetailFile findByFileKey(String fileKey) {
		return directorProfileDetailFileRepository.findByFileKey(fileKey)
			.orElseThrow(() -> new CustomRuntimeException(FileException.FILE_NOT_FOUND));
	}
}
