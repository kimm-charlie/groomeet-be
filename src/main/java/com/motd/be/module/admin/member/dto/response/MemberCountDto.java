package com.motd.be.module.admin.member.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberCountDto {
	private Long totalCount;
	private Long directorCount;
	private Long memberCount;
}
