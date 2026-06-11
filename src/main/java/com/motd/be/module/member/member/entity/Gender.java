package com.motd.be.module.member.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Gender {

	MAN("1"),
	WOMAN("2");

	private final String code;

	public static Gender findByCode(String code) {
		for (Gender gender : Gender.values()) {
			if (gender.getCode().equals(code)) {
				return gender;
			}
		}
		return null;
	}
}

