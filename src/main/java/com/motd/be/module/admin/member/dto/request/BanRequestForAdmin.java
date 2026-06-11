package com.motd.be.module.admin.member.dto.request;

import static com.motd.be.common.constants.Constants.*;

import com.motd.be.module.member.member.entity.BanPeriod;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class BanRequestForAdmin {

	@NotNull(message = BAN_PERIOD_REQUIRED)
	private BanPeriod banPeriod;
}
