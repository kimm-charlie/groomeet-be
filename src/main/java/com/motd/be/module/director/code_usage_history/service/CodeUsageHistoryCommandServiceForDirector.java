package com.motd.be.module.director.code_usage_history.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.code_usage_history.repository.CodeUsageHistoryRepositoryForDirector;
import com.motd.be.module.member.code_usage_history.entity.CodeUsageHistory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CodeUsageHistoryCommandServiceForDirector {

	private final CodeUsageHistoryRepositoryForDirector codeUsageHistoryRepositoryForDirector;

	public void save(CodeUsageHistory codeUsageHistory) {
		codeUsageHistoryRepositoryForDirector.save(codeUsageHistory);
	}
}
