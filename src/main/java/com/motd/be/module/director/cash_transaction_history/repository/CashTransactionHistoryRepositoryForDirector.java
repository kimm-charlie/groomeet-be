package com.motd.be.module.director.cash_transaction_history.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.cash.entity.CashTransactionType;
import com.motd.be.module.member.cash_transaction_history.entity.CashTransactionHistory;
import com.motd.be.module.member.member.entity.Member;

public interface CashTransactionHistoryRepositoryForDirector extends JpaRepository<CashTransactionHistory, Long> {

	@Query("""
			SELECT ch
			FROM CashTransactionHistory ch
			WHERE ch.member = :member
			  AND (:cashTransactionType IS NULL OR ch.cashTransactionType = :cashTransactionType)
			ORDER BY ch.createdAt DESC
		""")
	Slice<CashTransactionHistory> findAllByMember(@Param("member") Member member, Pageable pageable,
		@Param("cashTransactionType") CashTransactionType cashTransactionType);
}
