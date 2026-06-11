package com.motd.be.module.member.portfolio.validator;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.PortfolioException;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.portfolio.entity.Portfolio;

@Component
public class PortfolioValidator {

	/**
	 * [1] 권한 검증용 — 잘못된 경우 예외 던짐
	 */
	public void validateOwnership(Portfolio portfolio, DirectorInfo directorInfo) {
		if (!portfolio.isOwnedBy(directorInfo)) {
			throw new CustomRuntimeException(PortfolioException.NO_AUTHORITY);
		}
	}

	/**
	 * [2] 뷰용 판별 — 단순히 소유 여부만 boolean 으로 반환
	 */
	public boolean isOwnedByMember(DirectorInfo directorInfo, Portfolio portfolio) {
		return portfolio.isOwnedBy(directorInfo);
	}
}
