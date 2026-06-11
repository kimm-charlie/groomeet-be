package com.motd.be.provider.module.member;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.code_usage_history.entity.CodeUsageHistory;
import com.motd.be.module.member.code_usage_history.repository.CodeUsageHistoryRepository;
import com.motd.be.module.member.member.entity.Member;

@Component
public class CodeUsageHistoryProvider {

	@Autowired
	private CodeUsageHistoryRepository codeUsageHistoryRepository;

	public CodeUsageHistory saveWithInviterAndInvitee(Member inviter, Member invitee) {
		return codeUsageHistoryRepository.save(CodeUsageHistory.ofWithInviterAndInvitee(inviter, invitee));
	}

	public List<CodeUsageHistory> findAll() {
		return codeUsageHistoryRepository.findAll();
	}
}
