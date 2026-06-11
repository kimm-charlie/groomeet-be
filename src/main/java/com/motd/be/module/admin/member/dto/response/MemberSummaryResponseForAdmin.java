package com.motd.be.module.admin.member.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;
import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberSummaryResponseForAdmin {

	private Long id;
	private String nickname;
	private String profileImageUrl;
	private Boolean isDirector;
	private Boolean isBanned;
	private List<String> services;

	public static List<MemberSummaryResponseForAdmin> fromList(List<Member> members) {
		return members.stream()
			.map(MemberSummaryResponseForAdmin::from)
			.collect(Collectors.toList());
	}

	public static MemberSummaryResponseForAdmin from(Member member) {
		return MemberSummaryResponseForAdmin.builder()
			.id(member.getId())
			.nickname(member.getNickname())
			.profileImageUrl(member.getCdnProfileImageUrl())
			.isDirector(member.getIsDirector())
			.isBanned(member.getIsBanned())
			.services(extractParentServiceNames(member))
			.build();
	}

	private static List<String> extractParentServiceNames(Member member) {
		if (!member.isDirector()) {
			return null;
		}

		DirectorInfo directorInfo = member.getDirectorInfo();
		return directorInfo.getDirectorServiceMappings().stream()
			.filter(mapping -> mapping.getIsDeleted() == Boolean.FALSE)
			.map(DirectorServiceMapping::getDirectorService)
			.map(service -> service.getParent() != null ? service.getParent().getName() : service.getName())
			.distinct()
			.collect(Collectors.toList());
	}
}
