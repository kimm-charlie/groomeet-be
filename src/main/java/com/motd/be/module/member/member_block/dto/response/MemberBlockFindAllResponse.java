package com.motd.be.module.member.member_block.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.module.member.member_block.entity.MemberBlock;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberBlockFindAllResponse {

	private int page;
	private Boolean hasNext;
	private List<MemberResponse> blocks;

	public static MemberBlockFindAllResponse from(Slice<MemberBlock> blocker) {
		return MemberBlockFindAllResponse.builder()
			.page(blocker.getNumber())
			.hasNext(blocker.hasNext())
			.blocks(blocker.stream()
				.map(MemberBlock::getBlocked)
				.map(MemberResponse::from)
				.toList())
			.build();
	}
}
