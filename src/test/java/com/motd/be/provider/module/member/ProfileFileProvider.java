package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.profile_file.entity.ProfileFile;
import com.motd.be.module.member.profile_file.respository.ProfileFileRepository;
import com.motd.be.shared.aws.enums.UploadFileType;

@Component
public class ProfileFileProvider {

	@Autowired
	private ProfileFileRepository profileFileRepository;

	public ProfileFile save(Member member, String cdnUrl) {
		return profileFileRepository.save(ProfileFile.builder()
			.member(member)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(cdnUrl)
			.fileKey(FILE_KEY_STR)
			.sortOrder(0)
			.fileType(UploadFileType.IMAGE)
			.build());
	}

	public List<ProfileFile> findAll() {
		return profileFileRepository.findAll();
	}
}
