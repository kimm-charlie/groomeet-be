package com.motd.be.module.member.member_metadata.dto.response;

import com.motd.be.module.member.member_metadata.entity.MemberMetadata;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberMetadataFindResponse {

	private Long id;
	private String version;

	public static MemberMetadataFindResponse from(MemberMetadata byMemberAndDeviceType) {
		return MemberMetadataFindResponse.builder()
			.id(byMemberAndDeviceType.getId())
			.version(byMemberAndDeviceType.getVersion())
			.build();
	}
}
