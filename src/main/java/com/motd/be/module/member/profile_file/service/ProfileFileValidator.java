package com.motd.be.module.member.profile_file.service;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ProfileFileException;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.profile_file.entity.ProfileFile;

@Component
public class ProfileFileValidator {

	public void validateOwnership(Member member, ProfileFile profileFile) {
		if (!profileFile.getMember().getId().equals(member.getId())) {
			throw new CustomRuntimeException(ProfileFileException.NOT_OWNED_BY);
		}
	}
}
