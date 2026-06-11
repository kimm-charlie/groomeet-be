package com.motd.be.module.member.member_nickname_history.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motd.be.module.member.member_nickname_history.entity.MemberNicknameHistory;

public interface MemberNicknameHistoryRepository extends JpaRepository<MemberNicknameHistory, Long> {
}
