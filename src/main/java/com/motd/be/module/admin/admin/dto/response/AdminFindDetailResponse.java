package com.motd.be.module.admin.admin.dto.response;

import com.motd.be.module.admin.admin.entity.Admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminFindDetailResponse {

	private Long id;
	private String email;
	private String nickname;

	public static AdminFindDetailResponse from(Admin admin) {
		return AdminFindDetailResponse.builder()
			.id(admin.getId())
			.email(admin.getEmail())
			.nickname(admin.getNickname())
			.build();
	}
}
