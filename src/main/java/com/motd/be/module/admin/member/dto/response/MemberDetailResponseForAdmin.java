package com.motd.be.module.admin.member.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.motd.be.module.admin.director_service.dto.response.DirectorServiceChildrenResponseForAdmin;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;
import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberDetailResponseForAdmin {

	private Long id;
	private String nickname;
	private String profileImageUrl;
	private String phoneNumber;
	private String name;
	private String birth;
	private String createdAt;
	private String signInPlatform;
	private Boolean isDirector;
	private Boolean isAuthenticated;
	private Boolean isBanned;
	private String bannedAt;
	private String email;
	private Integer reportedCount;
	private List<DirectorServiceChildrenResponseForAdmin> services;

	public static MemberDetailResponseForAdmin from(Member member) {
		return MemberDetailResponseForAdmin.builder()
			.id(member.getId())
			.nickname(member.getNickname())
			.profileImageUrl(member.getCdnProfileImageUrl())
			.phoneNumber(member.getPhoneNumber())
			.name(member.getName())
			.birth(member.getBirth() != null ? formatToDateString(member.getBirth()) : null)
			.createdAt(member.getCreatedAt() != null ? formatToDateString(member.getCreatedAt()) : null)
			.signInPlatform(member.getSignInPlatform() != null ? member.getSignInPlatform().name() : null)
			.isDirector(member.getIsDirector())
			.isAuthenticated(member.getIsAuthenticated())
			.isBanned(member.getIsBanned())
			.bannedAt(member.getBannedAt() != null ? formatToDateString(member.getBannedAt()) : null)
			.email(member.getEmail())
			.reportedCount(member.getReportedCount())
			.services(extractDirectorServices(member))
			.build();
	}

	private static List<DirectorServiceChildrenResponseForAdmin> extractDirectorServices(Member member) {
		if (!member.isDirector()) {
			return null;
		}

		DirectorInfo directorInfo = member.getDirectorInfo();
		return directorInfo.getDirectorServiceMappings().stream()
			.filter(mapping -> !mapping.getIsDeleted())
			.map(DirectorServiceMapping::getDirectorService)
			.collect(Collectors.groupingBy(service -> service.getParent() == null ? service : service.getParent()))
			.entrySet().stream()
			.sorted(Comparator.comparing(entry -> entry.getKey().getSortOrder()))
			.map(entry -> DirectorServiceChildrenResponseForAdmin.of(
				entry.getKey().getId(),
				entry.getKey().getName(),
				extractChildServiceNames(entry.getValue())))
			.collect(Collectors.toList());
	}

	private static List<String> extractChildServiceNames(List<DirectorService> services) {
		return services.stream()
			.filter(s -> s.getParent() != null)
			.sorted(Comparator.comparing(DirectorService::getSortOrder))
			.map(DirectorService::getName)
			.collect(Collectors.toList());
	}
}

