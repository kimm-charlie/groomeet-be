package com.motd.be.module.admin.member.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberFindAllResponseForAdmin {

	private Integer page;
	private Boolean hasNext;
	private List<MemberSummaryResponseForAdmin> members;
	private Long totalCount;
	private Long directorCount;
	private Long memberCount;

	public static MemberFindAllResponseForAdmin from(Slice<Member> members, Long totalCount, Long directorCount, Long memberCount) {
		return MemberFindAllResponseForAdmin.builder()
			.page(members.getNumber())
			.hasNext(members.hasNext())
			.members(MemberSummaryResponseForAdmin.fromList(members.getContent()))
			.totalCount(totalCount)
			.directorCount(directorCount)
			.memberCount(memberCount)
			.build();
	}
}
