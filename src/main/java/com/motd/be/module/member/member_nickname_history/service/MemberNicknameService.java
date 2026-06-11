package com.motd.be.module.member.member_nickname_history.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_nickname_history.entity.MemberNicknameHistory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberNicknameService {

	private final MemberNicknameHistoryCommandService memberNicknameHistoryCommandService;

	public void recordNicknameChange(Member member, String oldNickname, String newNickname) {
		if (oldNickname.equals(newNickname)) {
			return;
		}

		memberNicknameHistoryCommandService.save(
			MemberNicknameHistory.of(member, oldNickname, newNickname));
	}
}
