package com.motd.be.module.director.director_profile_detail.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.director_profile_detail.dto.request.DirectorProfileUpdateRequestForDirector;
import com.motd.be.module.director.director_profile_detail.service.DirectorProfileDetailServiceForDirector;
import com.motd.be.module.director.director_profile_detail_file.service.DirectorProfileDetailFileServiceForDirector;
import com.motd.be.module.director.member.service.MemberQueryServiceForDirector;
import com.motd.be.module.member.director_info.service.DirectorInfoService;
import com.motd.be.module.member.director_profile_detail.entity.DirectorProfileDetail;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.shared.forbidden_word.validator.ForbiddenWordValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectorProfileDetailFacadeForDirector {

	private final DirectorProfileDetailServiceForDirector directorProfileDetailServiceForDirector;
	private final MemberQueryServiceForDirector memberQueryService;
	private final DirectorProfileDetailFileServiceForDirector directorProfileDetailFileServiceForDirector;
	private final DirectorInfoService directorInfoService;
	private final ForbiddenWordValidator forbiddenWordValidator;

	@Transactional
	public void update(Long memberId, DirectorProfileUpdateRequestForDirector request) {
		// 1. 디렉터 조회
		Member director = memberQueryService.findByIdWithDirector(memberId);

		forbiddenWordValidator.validate(request.getContentJson());

		// 2. 디렉터 프로필 상세 업데이트
		DirectorProfileDetail directorProfileDetail = directorProfileDetailServiceForDirector.update(
			director.getDirectorInfo(),
			request);

		// 3.이미지 매핑
		directorProfileDetailFileServiceForDirector.mapFiles(directorProfileDetail, request.getFileIds(), director);

		// 4. 디렉터 프로필 작성 여부 업데이트
		directorInfoService.updateIsProfileDetailExistToTrue(director);
	}
}

