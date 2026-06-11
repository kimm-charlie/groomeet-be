package com.motd.be.module.member.code_usage_history.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.code_usage_history.entity.CodeUsageHistory;
import com.motd.be.module.member.code_usage_history.repository.CodeUsageHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CodeUsageHistoryCommandService {

	private final CodeUsageHistoryRepository codeUsageHistoryRepository;

	public void save(CodeUsageHistory codeUsageHistory) {
		codeUsageHistoryRepository.save(codeUsageHistory);
	}
}
