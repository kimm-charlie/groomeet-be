package com.motd.be.module.member.profile_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.FileException;
import com.motd.be.exception.exceptions.ProfileFileException;
import com.motd.be.module.member.profile_file.entity.ProfileFile;
import com.motd.be.module.member.profile_file.respository.ProfileFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileFileQueryService {

	private final ProfileFileRepository profileFileRepository;

	public ProfileFile findByIdWithIsDeletedFalse(Long id) {
		return profileFileRepository.findByIdWithIsDeletedFalse(id)
			.orElseThrow(() -> new CustomRuntimeException(ProfileFileException.NOT_FOUND));
	}

	public List<ProfileFile> findAllByIds(List<Long> ids) {
		return profileFileRepository.findAllByIds(ids);
	}

	public ProfileFile findByFileKey(String fileKey) {
		return profileFileRepository.findByFileKey(fileKey)
			.orElseThrow(() -> new CustomRuntimeException(FileException.FILE_NOT_FOUND));
	}
}
