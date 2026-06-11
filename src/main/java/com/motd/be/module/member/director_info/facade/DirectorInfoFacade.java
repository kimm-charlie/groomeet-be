package com.motd.be.module.member.director_info.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.director_info.dto.request.DirectorInfoRegisterRequest;
import com.motd.be.module.member.director_info.dto.response.DirectorRankMainViewResponse;
import com.motd.be.module.member.director_info.dto.response.DirectorRankPageResponse;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_info.service.DirectorInfoService;
import com.motd.be.module.member.director_info.validator.DirectorInfoValidator;
import com.motd.be.module.member.director_location_mapping.service.DirectorLocationMappingService;
import com.motd.be.module.member.director_service_mapping.service.DirectorServiceMappingService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.member.service.MemberService;
import com.motd.be.module.member.member_nickname_history.service.MemberNicknameService;
import com.motd.be.module.member.refresh_token.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectorInfoFacade {

	private final MemberQueryService memberQueryService;
	private final DirectorInfoService directorInfoService;
	private final DirectorLocationMappingService directorLocationMappingService;
	private final DirectorServiceMappingService directorServiceMappingService;
	private final MemberService memberService;
	private final DirectorInfoValidator directorInfoValidator;
	private final RefreshTokenService refreshTokenService;
	private final MemberNicknameService memberNicknameService;

	@Transactional
	public Jwt register(Long memberId, DirectorInfoRegisterRequest request, String accessToken, String refreshToken) {
		// 1. 회원 조회
		Member member = memberQueryService.findByIdWithLock(memberId);

		// 2. 디렉터 중복 등록 방지 검증
		directorInfoValidator.validateNotAlreadyDirector(member);

		//닉네임 변경 기록 저장
		memberNicknameService.recordNicknameChange(member, member.getNickname(), request.getNickname());

		// 3. 닉네임 업데이트
		memberService.updateNickname(member, request.getNickname());

		// 4. 디렉터 정보 생성
		DirectorInfo directorInfo = directorInfoService.createDirectorInfo(request);

		// 5. 지역 매핑 생성
		directorLocationMappingService.createMappings(directorInfo, request.getLocationIds());

		// 6. 디렉터 서비스(카테고리) 매핑 생성
		directorServiceMappingService.createMappings(directorInfo, request.getDirectorServiceIds());

		// 7. 회원 → 디렉터 전환
		memberService.registerAsDirector(member, directorInfo);

		// 8. 토큰 재발급 (access / refresh)
		return refreshTokenService.reissueTokens(member, accessToken, refreshToken);
	}

	public DirectorRankMainViewResponse findDirectorRankInMainView() {
		return directorInfoService.findDirectorRankInMainView();
	}

	public DirectorRankPageResponse findDirectorRankInRankView(int page) {
		return directorInfoService.findDirectorRankInRankView(page);
	}
}
