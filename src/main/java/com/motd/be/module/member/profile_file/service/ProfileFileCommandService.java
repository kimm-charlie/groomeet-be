package com.motd.be.module.member.profile_file.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.file.entity.BaseFile;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.profile_file.entity.ProfileFile;
import com.motd.be.module.member.profile_file.respository.ProfileFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileFileCommandService {

	private final ProfileFileRepository profileFileRepository;

	public void deleteProfileFileWhenUpdate(Member member, ProfileFile excludingProfileFile) {
		profileFileRepository.deleteByMemberAndIdNot(member, excludingProfileFile.getId());
	}

	public void deleteAllProfileFilesOfMember(Member member) {
		profileFileRepository.deleteByMember(member);
	}

	public BaseFile save(ProfileFile entity) {
		return profileFileRepository.save(entity);
	}
}
