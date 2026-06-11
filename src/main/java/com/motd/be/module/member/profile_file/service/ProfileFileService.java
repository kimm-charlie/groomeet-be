package com.motd.be.module.member.profile_file.service;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ProfileFileException;
import com.motd.be.module.member.member.dto.request.MemberUpdateProfileImageRequest;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.profile_file.entity.ProfileFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileFileService {

	private final ProfileFileQueryService profileFileQueryService;
	private final ProfileFileValidator profileFileValidator;

	public ProfileFile find(Member member, MemberUpdateProfileImageRequest request) {
		if (request.getToDefault()) {
			return null;
		}

		if (request.getFileId() == null) {
			throw new CustomRuntimeException(ProfileFileException.FILE_ID_IS_NULL);
		}

		ProfileFile profileFile = profileFileQueryService.findByIdWithIsDeletedFalse(request.getFileId());

		profileFileValidator.validateOwnership(member, profileFile);

		return profileFile;
	}
}
