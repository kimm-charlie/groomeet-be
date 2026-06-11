package com.motd.be.module.director.portfolio.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.director_info.service.DirectorInfoServiceForDirector;
import com.motd.be.module.director.director_service.service.DirectorServiceServiceForDirector;
import com.motd.be.module.director.member.service.MemberQueryServiceForDirector;
import com.motd.be.module.director.member_director_favorite.service.MemberDirectorFavoriteQueryServiceForDirector;
import com.motd.be.module.director.notification.service.NotificationServiceForDirector;
import com.motd.be.module.director.portfolio.dto.request.PortfolioSaveAndUpdateRequestForDirector;
import com.motd.be.module.director.portfolio.service.PortfolioCommandServiceForDirector;
import com.motd.be.module.director.portfolio.service.PortfolioQueryServiceForDirector;
import com.motd.be.module.director.portfolio.service.PortfolioServiceForDirector;
import com.motd.be.module.director.portfolio_file.service.PortfolioFileServiceForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.motd.be.shared.forbidden_word.validator.ForbiddenWordValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioFacadeForDirector {

	private final MemberQueryServiceForDirector memberQueryServiceForDirector;
	private final PortfolioServiceForDirector portfolioServiceForDirector;
	private final PortfolioCommandServiceForDirector portfolioCommandServiceForDirector;
	private final DirectorServiceServiceForDirector directorServiceServiceForDirector;
	private final DirectorInfoServiceForDirector directorInfoServiceForDirector;
	private final PortfolioQueryServiceForDirector portfolioQueryServiceForDirector;
	private final NotificationServiceForDirector notificationServiceForDirector;
	private final PortfolioFileServiceForDirector portfolioFileServiceForDirector;
	private final MemberDirectorFavoriteQueryServiceForDirector memberDirectorFavoriteQueryServiceForDirector;
	private final ForbiddenWordValidator forbiddenWordValidator;

	@Transactional
	public void save(Long memberId, PortfolioSaveAndUpdateRequestForDirector request) {
		// 1. 디렉터 회원 조회
		Member director = memberQueryServiceForDirector.findByIdWithDirector(memberId);
		DirectorInfo directorInfo = director.getDirectorInfo();

		// 2. 디렉터 서비스 조회
		DirectorService directorService = directorServiceServiceForDirector.findByIdWithValidation(directorInfo,
			request.getDirectorServiceId());

		// 3. 금칙어 검증
		forbiddenWordValidator.validateAll(request.getTitle(), request.getContent());

		// 4. 포트폴리오 저장
		Portfolio savedPortfolio = portfolioCommandServiceForDirector.save(
			request.toEntity(directorInfo, directorService)
		);

		// 6. 포트폴리오 이미지 매핑 (대표 이미지 여부 포함)
		portfolioFileServiceForDirector.mapImagesToPortfolio(savedPortfolio, request.getThumbnailImageId(),
			request.getFileIds(), director);

		// 7. 디렉터 정보 업데이트
		directorInfoServiceForDirector.updateIsPortfolioExistWhenSave(directorInfo);

		// 8. 즐겨찾기한 회원들에게 알림 전송
		List<Member> favoriteMembers = memberDirectorFavoriteQueryServiceForDirector.findAllMembersByTargetMemberId(
			director.getId());

		notificationServiceForDirector.savePortfolioUploadedNotification(
			director,
			favoriteMembers,
			savedPortfolio.getId()
		);

		// push 전송
		portfolioServiceForDirector.sendPushToFavoriteMembersWhenPortfolioUpload(favoriteMembers, director,
			savedPortfolio);
	}

	@Transactional
	public void update(Long memberId, Long portfolioId, PortfolioSaveAndUpdateRequestForDirector request) {
		// 1. 디렉터 회원 조회
		Member director = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		// 2. 디렉터 서비스 조회
		DirectorService directorService = directorServiceServiceForDirector.findByIdWithValidation(
			director.getDirectorInfo(),
			request.getDirectorServiceId());

		// 3. 금칙어 검증
		forbiddenWordValidator.validateAll(request.getTitle(), request.getContent());

		// 4. 포트폴리오 수정 (내부 검증 + 업데이트)
		Portfolio portfolio = portfolioQueryServiceForDirector.findById(portfolioId);
		portfolioServiceForDirector.update(directorService, director, portfolio, request);

		// 5. 포트폴리오 이미지 업데이트
		portfolioFileServiceForDirector.updateImage(portfolio, request.getFileIds(), request.getThumbnailImageId(),
			director);
	}

	@Transactional
	public void delete(Long memberId, Long portfolioId) {
		// 1. 디렉터 회원 조회
		Member member = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		// 2. 포트폴리오 삭제 (내부 검증 포함)
		portfolioServiceForDirector.delete(member.getDirectorInfo(), portfolioId);

		// 3. 디렉터 정보 업데이트
		directorInfoServiceForDirector.updateIsPortfolioExistWhenDelete(member.getDirectorInfo());
	}
}

