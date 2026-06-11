package com.motd.be.module.member.director_profile_detail_file.validator;

import java.util.List;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.DirectorProfileDetailFileException;
import com.motd.be.module.member.director_profile_detail_file.entity.DirectorProfileDetailFile;
import com.motd.be.module.member.member.entity.Member;

@Component
public class DirectorProfileDetailFileValidator {

	public void validateFileCount(List<DirectorProfileDetailFile> newFiles, List<Long> fileIds) {
		if (newFiles.size() != fileIds.size()) {
			throw new CustomRuntimeException(DirectorProfileDetailFileException.INVALID_IMAGE_COUNT);
		}
	}

	public void validateFileOwnerShip(List<DirectorProfileDetailFile> newFiles, Member director) {
		newFiles.forEach(newFile -> {
			if (!newFile.getMember().getId().equals(director.getId())) {
				throw new CustomRuntimeException(DirectorProfileDetailFileException.INVALID_FILE_OWNER_SHIP);
			}
		});
	}
}
