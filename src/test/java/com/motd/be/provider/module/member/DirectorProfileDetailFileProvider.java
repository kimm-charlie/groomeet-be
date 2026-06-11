package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.director_profile_detail.entity.DirectorProfileDetail;
import com.motd.be.module.member.director_profile_detail_file.entity.DirectorProfileDetailFile;
import com.motd.be.module.member.director_profile_detail_file.repository.DirectorProfileDetailFileRepository;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.shared.aws.enums.UploadFileType;

@Component
public class DirectorProfileDetailFileProvider {

	@Autowired
	private DirectorProfileDetailFileRepository directorProfileDetailFileRepository;

	public DirectorProfileDetailFile save(Member member) {
		return directorProfileDetailFileRepository.save(DirectorProfileDetailFile.builder()
			.member(member)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.sortOrder(0)
			.fileKey(FILE_KEY_STR)
			.fileType(UploadFileType.IMAGE)
			.build());
	}

	public DirectorProfileDetailFile saveWithProfileDetail(Member member, DirectorProfileDetail directorProfileDetail) {
		return directorProfileDetailFileRepository.save(DirectorProfileDetailFile.builder()
			.member(member)
			.directorProfileDetail(directorProfileDetail)
			.originUrl(IMAGE_URL_STR)
			.cdnUrl(IMAGE_URL_STR)
			.sortOrder(0)
			.fileKey(FILE_KEY_STR)
			.fileType(UploadFileType.IMAGE)
			.build());
	}
}
