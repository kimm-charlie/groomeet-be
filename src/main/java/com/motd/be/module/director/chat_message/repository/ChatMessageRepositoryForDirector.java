package com.motd.be.module.director.chat_message.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motd.be.module.member.chat_message.entity.ChatMessage;

public interface ChatMessageRepositoryForDirector extends JpaRepository<ChatMessage, Long> {
}
