package com.motd.be.module.member.director_profile_detail.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.director_profile_detail.dto.response.DirectorProfileFindDetailResponseForPublic;
import com.motd.be.module.member.director_profile_detail.facade.DirectorProfileDetailFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DirectorProfileDetailController {

	private final DirectorProfileDetailFacade directorProfileDetailFacade;

	/**
	 * 디렉터의 프로필 상세를 조회하는 API (공개용)
	 *
	 * @param memberId
	 * @return
	 */
	@GetMapping("/directors/{memberId}/profile-detail")
	public ResponseEntity<DirectorProfileFindDetailResponseForPublic> findDetail(
		@PathVariable(MEMBER_ID) Long memberId) {
		return ResponseEntity.status(HttpStatus.OK).body(directorProfileDetailFacade.findDetail(memberId));
	}

}
