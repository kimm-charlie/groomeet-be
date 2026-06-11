package com.motd.be.module.member.member_director_favorite.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;

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
public class MemberDirectorFavoriteRequest {

	@NotNull(message = TARGET_ID_REQUIRED)
	private Long targetMemberId;
}

