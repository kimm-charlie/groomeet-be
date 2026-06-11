package com.motd.be.module.member.member_block.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_block.entity.MemberBlock;
import com.motd.be.module.member.member_block.repository.MemberBlockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberBlockQueryService {

	private final MemberBlockRepository memberBlockRepository;

	/**
	 * member 가 target 을 차단했거나
	 * target 이 member 를 차단했는지 여부
	 *
	 * @param member
	 * @param target
	 * @return
	 */
	public boolean existsByBlockerOrBlocked(Member member, Member target) {
		return memberBlockRepository.existsByBlockerAndBlocked(member, target);
	}

	/**
	 * member 가 blockedId 를 차단했는지 여부
	 *
	 * @param blockerId
	 * @param blockedId
	 * @return
	 */
	public boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId) {
		return memberBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId);
	}

	public Slice<MemberBlock> findAllByBlocker(Member member, Pageable pageable) {
		return memberBlockRepository.findAllByBlocker(member, pageable);
	}

	public List<Long> findAllBlockRelatedMemberIds(Long memberId) {
		return memberBlockRepository.findAllBlockRelatedMemberIds(memberId);
	}

}
