package com.motd.be.module.member.chat_file.entity;

import static com.motd.be.shared.aws.util.ImageUrlConverter.*;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.file.entity.BaseFile;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class ChatFile extends BaseFile {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_message_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private ChatMessage chatMessage;

	@Builder
	public ChatFile(ChatMessage chatMessage, Member member, String originUrl, String cdnUrl, String fileKey,
		Integer sortOrder, Boolean isDeleted, UploadFileType fileType, String fileName, String fileSize) {
		this.chatMessage = chatMessage;
		this.member = member;
		this.originUrl = originUrl;
		this.cdnUrl = cdnUrl;
		this.fileKey = fileKey;
		this.sortOrder = sortOrder;
		this.isDeleted = isDeleted;
		this.fileType = fileType;
		this.fileName = fileName;
		this.fileSize = fileSize;
	}

	public static ChatFile ofWithoutChatMessage(String originUrl, String fileKey, Member member,
		UploadFileType fileType, String fileName, String fileSize) {
		return ChatFile.builder()
			.member(member)
			.originUrl(originUrl)
			.cdnUrl(toCdnUrl(originUrl))
			.fileKey(fileKey)
			.sortOrder(0)
			.fileType(fileType)
			.fileName(fileName)
			.fileSize(fileSize)
			.build();
	}
}
