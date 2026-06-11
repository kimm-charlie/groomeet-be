package com.motd.be.module.member.prompt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motd.be.module.member.prompt.entity.PromptMessage;
import com.motd.be.module.member.prompt.entity.PromptRoom;

public interface PromptMessageRepository extends JpaRepository<PromptMessage, Long> {

	List<PromptMessage> findAllByPromptRoomOrderByIdAsc(PromptRoom promptRoom);
}
