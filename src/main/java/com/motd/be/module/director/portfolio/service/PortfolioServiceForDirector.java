package com.motd.be.module.director.portfolio.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.portfolio.dto.request.PortfolioSaveAndUpdateRequestForDirector;
import com.motd.be.module.director.portfolio_file.service.PortfolioFileCommandServiceForDirector;
import com.motd.be.module.director.portfolio_file.service.PortfolioFileQueryServiceForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.motd.be.module.member.portfolio.validator.PortfolioValidator;
import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;
import com.motd.be.shared.firebase.dto.FirebasePushEvent;
import com.motd.be.shared.firebase.service.FirebaseEventPublisher;
import com.motd.be.shared.firebase.service.FirebasePushFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioServiceForDirector {

	private final PortfolioQueryServiceForDirector portfolioQueryServiceForDirector;
	private final PortfolioFileQueryServiceForDirector portfolioFileQueryService;
	private final PortfolioValidator portfolioValidator;
	private final PortfolioFileCommandServiceForDirector portfolioFileCommandService;
	private final FirebaseEventPublisher firebaseEventPublisher;
	private final FirebasePushFactory firebasePushFactory;

	public void update(DirectorService directorService, Member member, Portfolio portfolio,
		PortfolioSaveAndUpdateRequestForDirector request) {
		// 1. 수정 권한 검증
		portfolioValidator.validateOwnership(portfolio, member.getDirectorInfo());

		// 4. 기타 필드 변경
		portfolio.update(request.getTitle(), request.getContent(), directorService, request.getPrice());
	}

	public void delete(DirectorInfo directorInfo, Long portfolioId) {
		Portfolio portfolio = portfolioQueryServiceForDirector.findById(portfolioId);

		// 1.  포트폴리오 삭제 권한이 있는 디렉터인지 검증
		portfolioValidator.validateOwnership(portfolio, directorInfo);

		// 2. 포트폴리오 삭제
		portfolio.delete();

		// 3. 포트폴리오 이미지 삭제
		List<PortfolioFile> portfolioImages = portfolioFileQueryService.findAllByPortfolioId(portfolioId);
		portfolioFileCommandService.softDeleteAll(portfolioImages);
	}

	public void sendPushToFavoriteMembersWhenPortfolioUpload(List<Member> favoriteMembers, Member director,
		Portfolio savedPortfolio) {
		List<Member> pushAgreedMembers = favoriteMembers.stream()
			.filter(Member::getIsActivityPushAgreed)
			.toList();

		FirebasePushEvent event = firebasePushFactory.favoritePortfolioUploaded(director, pushAgreedMembers,
			savedPortfolio);

		firebaseEventPublisher.sendPush(event);
	}
}
