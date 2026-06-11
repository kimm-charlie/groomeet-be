package com.motd.be.module.member.member_nickname_history.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.member_nickname_history.entity.MemberNicknameHistory;
import com.motd.be.module.member.member_nickname_history.repository.MemberNicknameHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberNicknameHistoryCommandService {

	private final MemberNicknameHistoryRepository memberNicknameHistoryRepository;

	public void save(MemberNicknameHistory memberNicknameHistory) {
		memberNicknameHistoryRepository.save(memberNicknameHistory);
	}
}
