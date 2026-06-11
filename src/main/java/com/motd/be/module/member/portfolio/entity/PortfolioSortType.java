package com.motd.be.module.member.portfolio.entity;

import lombok.Getter;

@Getter
public enum PortfolioSortType {
	LATEST, MOST_HIRED;

	public static PortfolioSortType from(String value) {
		for (PortfolioSortType sortType : values()) {
			if (sortType.name().equalsIgnoreCase(value)) {
				return sortType;
			}
		}
		return LATEST;
	}
}
