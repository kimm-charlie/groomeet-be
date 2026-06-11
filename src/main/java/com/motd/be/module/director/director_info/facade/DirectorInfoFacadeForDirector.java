package com.motd.be.module.director.director_info.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.director_info.dto.response.DirectorInfoFindProfileBasicInfoResponseForDirector;
import com.motd.be.module.director.director_info.dto.response.DirectorInfoFindProfileCompletenessResponseForDirector;
import com.motd.be.module.director.director_info.service.DirectorInfoServiceForDirector;
import com.motd.be.module.director.member.service.MemberQueryServiceForDirector;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.shared.forbidden_word.validator.ForbiddenWordValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectorInfoFacadeForDirector {

	private final MemberQueryServiceForDirector memberQueryService;
	private final DirectorInfoServiceForDirector directorInfoService;
	private final ForbiddenWordValidator forbiddenWordValidator;

	@Transactional
	public void updateIntroduceText(Long memberId, String introduceText) {
		// 1. 디렉터 회원 조회
		Member member = memberQueryService.findByIdWithDirector(memberId);

		// 2. 금칙어 검증
		forbiddenWordValidator.validate(introduceText);

		// 3. 자기소개 업데이트
		directorInfoService.updateIntroduceText(member.getDirectorInfo(), introduceText);
	}

	@Transactional
	public void updateStoreAddress(Long memberId, String storeAddress) {
		// 1. 디렉터 회원 조회
		Member member = memberQueryService.findByIdWithDirector(memberId);

		// 2. 금칙어 검증
		forbiddenWordValidator.validate(storeAddress);

		// 3. 스토어 주소 업데이트
		directorInfoService.updateStoreAddress(member.getDirectorInfo(), storeAddress);
	}

	public DirectorInfoFindProfileCompletenessResponseForDirector findProfileCompleteness(Long memberId) {
		return DirectorInfoFindProfileCompletenessResponseForDirector.of(
			memberQueryService.findByIdWithDirector(memberId).getDirectorInfo(), memberId);
	}

	public DirectorInfoFindProfileBasicInfoResponseForDirector findProfileBasicInfo(Long memberId) {
		Member member = memberQueryService.findByIdWithDirector(memberId);
		return DirectorInfoFindProfileBasicInfoResponseForDirector.from(member.getDirectorInfo());
	}

}
