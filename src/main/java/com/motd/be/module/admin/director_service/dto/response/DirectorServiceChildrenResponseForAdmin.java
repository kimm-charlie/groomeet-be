package com.motd.be.module.admin.director_service.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * 회원 조회용 - 담당 서비스(자식 서비스) 응답 DTO
 */
@Getter
@Builder
public class DirectorServiceChildrenResponseForAdmin {

	private Long id;
	private String name;
	private List<String> children;

	public static DirectorServiceChildrenResponseForAdmin of(Long id, String name, List<String> children) {
		return DirectorServiceChildrenResponseForAdmin.builder()
			.id(id)
			.name(name)
			.children(children)
			.build();
	}
}
