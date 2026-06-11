package com.motd.be.module.member.member_location_mapping.dto.request;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class MemberLocationMappingUpdateRequest {

	private List<Long> locationIds;
}
