package com.motd.be.module.member.member_block.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.member_block.entity.MemberBlock;
import com.motd.be.module.member.member_block.repository.MemberBlockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberBlockCommandService {

	private final MemberBlockRepository memberBlockRepository;

	public void save(MemberBlock entity) {
		memberBlockRepository.save(entity);
	}

	public void deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId) {
		memberBlockRepository.deleteByBlockerIdAndBlockedId(blockerId, blockedId);
	}

	public void deleteAllByMemberId(Long memberId) {
		memberBlockRepository.deleteAllByMemberId(memberId);
	}
}
