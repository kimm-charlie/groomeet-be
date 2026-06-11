package com.motd.be.module.director.cash.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.time.LocalDate;

import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CashFindResponseForDirector {

	private Long cash;
	private String onboardingPassEndsAt;

	public static CashFindResponseForDirector from(Member director) {
		LocalDate endsAt = director.getDirectorInfo().getOnboardingPassEndsAt();
		LocalDate today = LocalDate.now();

		String formatted =
			(endsAt != null && !today.isAfter(endsAt))
				? formatToDateString(endsAt)
				: null;

		return CashFindResponseForDirector.builder()
			.cash(director.getCashBalance())
			.onboardingPassEndsAt(formatted)
			.build();
	}
}
