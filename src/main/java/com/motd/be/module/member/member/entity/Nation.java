package com.motd.be.module.member.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Nation {

	DOMESTIC("0"),
	FOREIGN("1");

	private final String code;

	public static Nation findByCode(String code) {
		for (Nation nation : Nation.values()) {
			if (nation.getCode().equals(code)) {
				return nation;
			}
		}
		return null;
	}
}
