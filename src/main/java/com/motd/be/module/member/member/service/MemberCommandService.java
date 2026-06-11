package com.motd.be.module.member.member.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberCommandService {

	private final MemberRepository memberRepository;

	public Member save(Member member) {
		return memberRepository.save(member);
	}

	public void leaveAllChatRoomsByMember(Member member) {
		memberRepository.leaveAllChatRoomsByMember(member);
	}

	public int unbanMembers(LocalDate today) {
		return memberRepository.unbanMembers(today);
	}
}
