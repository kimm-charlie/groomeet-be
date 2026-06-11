package com.motd.be.module.member.chat_file.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.chat_file.entity.ChatFile;
import com.motd.be.module.member.chat_message.entity.ChatMessage;

public interface ChatFileRepository extends JpaRepository<ChatFile, Long> {

	@Query("""
		        SELECT cf
		        FROM ChatFile cf
		        WHERE cf.id IN :fileIds
		""")
	List<ChatFile> findAllByIds(List<Long> fileIds);

	@Query("""
			SELECT cf
			FROM ChatFile cf
			WHERE cf.fileKey = :fileKey
			AND cf.isDeleted = false
		""")
	Optional<ChatFile> findByFileKey(String fileKey);

	@Modifying
	@Query("""
			UPDATE ChatFile cf
			SET cf.chatMessage = :chatMessage
			WHERE cf IN :chatFiles
		""")
	void mapChatMessageByIds(@Param("chatMessage") ChatMessage chatMessage,
		@Param("chatFiles") List<ChatFile> chatFiles);
}
