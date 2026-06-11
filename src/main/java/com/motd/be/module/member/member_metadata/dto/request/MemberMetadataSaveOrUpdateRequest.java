package com.motd.be.module.member.member_metadata.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;
import static com.motd.be.common.utils.Utils.*;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_metadata.entity.DeviceType;
import com.motd.be.module.member.member_metadata.entity.MemberMetadata;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class MemberMetadataSaveOrUpdateRequest {

	@NotBlank(message = DEVICE_TYPE_REQUIRED)
	private String deviceType;
	@NotBlank(message = VERSION_REQUIRED)
	private String version;

	public MemberMetadata toEntity(Member member, DeviceType deviceType) {
		return MemberMetadata.builder()
			.member(member)
			.deviceType(deviceType)
			.version(version)
			.activeUniqueKey(generateMemberMetadataUniqueKey(member.getId(), deviceType))
			.build();
	}
}
