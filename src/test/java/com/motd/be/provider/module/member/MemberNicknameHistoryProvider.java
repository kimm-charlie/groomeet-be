package com.motd.be.provider.module.member;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.member_nickname_history.entity.MemberNicknameHistory;
import com.motd.be.module.member.member_nickname_history.repository.MemberNicknameHistoryRepository;

@Component
public class MemberNicknameHistoryProvider {

	@Autowired
	private MemberNicknameHistoryRepository memberNicknameHistoryRepository;

	public List<MemberNicknameHistory> findAll() {
		return memberNicknameHistoryRepository.findAll();
	}
}
