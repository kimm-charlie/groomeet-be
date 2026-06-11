package com.motd.be.module.member.prompt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motd.be.module.member.prompt.entity.PromptRoom;

public interface PromptRoomRepository extends JpaRepository<PromptRoom, Long> {
}
