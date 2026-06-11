package com.motd.be.module.director.code_usage_history.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motd.be.module.member.code_usage_history.entity.CodeUsageHistory;

public interface CodeUsageHistoryRepositoryForDirector extends JpaRepository<CodeUsageHistory, Long> {

}
