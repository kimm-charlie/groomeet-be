package com.motd.be.module.admin.chat_message.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motd.be.module.member.chat_message.entity.ChatMessage;

public interface ChatMessageRepositoryForAdmin extends JpaRepository<ChatMessage, Long> {
}
