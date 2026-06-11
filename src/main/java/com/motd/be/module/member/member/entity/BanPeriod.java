package com.motd.be.module.member.member.entity;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BanPeriod {
	ONE_DAY(1),
	THREE_DAYS(3),
	SEVEN_DAYS(7),
	THIRTY_DAYS(30),
	PERMANENT(null);

	private final Integer days;

	public LocalDate calculateUnbannedAt() {
		if (this.days == null) {
			return null;
		}
		return LocalDate.now().plusDays(this.days);
	}
}
