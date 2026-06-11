package com.motd.be.shared.firebase.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motd.be.shared.firebase.entity.FirebaseOutboundLog;

public interface FirebaseOutboundLogRepository extends JpaRepository<FirebaseOutboundLog, Long> {
}
