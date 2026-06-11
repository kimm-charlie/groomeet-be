package com.motd.be.provider.module.member;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_block.entity.MemberBlock;
import com.motd.be.module.member.member_block.repository.MemberBlockRepository;

@Component
public class MemberBlockProvider {

	@Autowired
	private MemberBlockRepository memberBlockRepository;

	public List<MemberBlock> findAll() {
		return memberBlockRepository.findAll();
	}

	public void save(Member blocker, Member blocked) {
		memberBlockRepository.save(MemberBlock.builder()
			.blocked(blocked)
			.blocker(blocker)
			.build());
	}
}
