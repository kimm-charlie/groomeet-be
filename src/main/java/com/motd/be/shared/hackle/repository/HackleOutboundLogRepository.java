package com.motd.be.shared.hackle.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motd.be.shared.hackle.entity.HackleOutboundLog;

public interface HackleOutboundLogRepository extends JpaRepository<HackleOutboundLog, Long> {
}
