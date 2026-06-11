package com.motd.be.module.director.director_profile_detail_file.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.director_profile_detail.entity.DirectorProfileDetail;
import com.motd.be.module.member.director_profile_detail_file.entity.DirectorProfileDetailFile;
import com.motd.be.module.member.director_profile_detail_file.validator.DirectorProfileDetailFileValidator;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectorProfileDetailFileServiceForDirector {

	private final DirectorProfileDetailFileQueryServiceForDirector directorProfileDetailFileQueryServiceForDirector;
	private final DirectorProfileDetailFileCommandServiceForDirector directorProfileDetailFileCommandServiceForDirector;
	private final DirectorProfileDetailFileValidator directorProfileDetailFileValidator;

	public void mapFiles(DirectorProfileDetail directorProfileDetail, List<Long> fileIds, Member director) {
		// 기존 이미지 조회
		List<DirectorProfileDetailFile> originalFiles = directorProfileDetail.getFiles();

		if (fileIds == null || fileIds.isEmpty()) {
			if (!(originalFiles == null || originalFiles.isEmpty())) {
				directorProfileDetailFileCommandServiceForDirector.deleteAllByIds(
					originalFiles.stream().map(DirectorProfileDetailFile::getId).collect(Collectors.toSet()));
			}
			return;
		}

		// 새로운 이미지 전체 조회
		List<DirectorProfileDetailFile> newFiles = directorProfileDetailFileQueryServiceForDirector
			.findAllByIds(fileIds);

		// 새로운 이미지 작성자 검증
		directorProfileDetailFileValidator.validateFileOwnerShip(newFiles, director);

		// 새로운 이미지 갯수 검증
		directorProfileDetailFileValidator.validateFileCount(newFiles, fileIds);

		// 기존 id 리스트
		Set<Long> originalIds = originalFiles.stream()
			.map(DirectorProfileDetailFile::getId)
			.collect(Collectors.toSet());

		// 새로운 id 리스트
		Set<Long> newIds = newFiles.stream()
			.map(DirectorProfileDetailFile::getId)
			.collect(Collectors.toSet());

		// 삭제해야 할 이미지 찾기: 기존 - 새로운
		Set<Long> deleteIds = originalIds.stream()
			.filter(id -> !newIds.contains(id))
			.collect(Collectors.toSet());

		// 추가해야 할 이미지 찾기: 새로운 - 기존
		Set<Long> addIds = newIds.stream()
			.filter(id -> !originalIds.contains(id))
			.collect(Collectors.toSet());

		// 이미지 한번에 삭제 처리
		directorProfileDetailFileCommandServiceForDirector.deleteAllByIds(deleteIds);

		// 이미지 한번에 매핑 처리
		directorProfileDetailFileCommandServiceForDirector.mapFilesToDirectorProfileDetail(directorProfileDetail,
			addIds);

	}
}
