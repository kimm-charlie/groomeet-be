package com.motd.be.module.member.chat_message.entity;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.chat_file.entity.ChatFile;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.review.entity.Review;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
@EqualsAndHashCode
public class ChatMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false, name = "chat_room_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private ChatRoom chatRoom;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "review_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Review review;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private ChatRoomMember chatRoomMember;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_estimate_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private ServiceEstimate serviceEstimate;
	@Enumerated(EnumType.STRING)
	private ChatMessageType messageType;
	private String content;
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isDeleted;
	@Column(nullable = false, columnDefinition = "boolean default true")
	private Boolean isVisibleToOpponent;
	@Column(updatable = false)
	private LocalDateTime sendAt;
	@OneToMany(mappedBy = "chatMessage", fetch = FetchType.LAZY)
	private List<ChatFile> images;

	@Builder
	public ChatMessage(ChatRoom chatRoom,
		ChatRoomMember chatRoomMember, ChatMessageType messageType,
		String content, Boolean isDeleted, LocalDateTime sendAt, ServiceEstimate serviceEstimate,
		Boolean isVisibleToOpponent, Review review, List<ChatFile> images) {
		this.chatRoom = chatRoom;
		this.chatRoomMember = chatRoomMember;
		this.serviceEstimate = serviceEstimate;
		this.messageType = messageType;
		this.content = content;
		this.isDeleted = isDeleted;
		this.isVisibleToOpponent = isVisibleToOpponent;
		this.sendAt = sendAt;
		this.review = review;
		this.images = images;
	}

	public static ChatMessage ofWithText(ChatRoom chatRoom, ChatRoomMember chatRoomMember, String content,
		ChatMessageType chatMessageType) {
		return ChatMessage.builder()
			.chatRoom(chatRoom)
			.chatRoomMember(chatRoomMember)
			.messageType(chatMessageType)
			.content(content)
			.sendAt(LocalDateTime.now())
			.isVisibleToOpponent(true)
			.build();
	}

	public static ChatMessage ofWithReview(ChatRoom chatRoom, ChatRoomMember chatRoomMember,
		Review review, ChatMessageType chatMessageType) {
		return ChatMessage.builder()
			.chatRoom(chatRoom)
			.chatRoomMember(chatRoomMember)
			.content(chatMessageType.getDisplayText())
			.review(review)
			.messageType(chatMessageType)
			.sendAt(LocalDateTime.now())
			.isVisibleToOpponent(true)
			.build();
	}

	public static ChatMessage ofWithEstimate(ChatRoom chatRoom, ChatRoomMember chatRoomMember,
		ServiceEstimate serviceEstimate, ChatMessageType chatMessageType) {
		return ChatMessage.builder()
			.chatRoom(chatRoom)
			.chatRoomMember(chatRoomMember)
			.content(chatMessageType.getDisplayText())
			.messageType(chatMessageType)
			.serviceEstimate(serviceEstimate)
			.sendAt(LocalDateTime.now())
			.isVisibleToOpponent(true)
			.build();
	}

	public static ChatMessage ofWithFile(ChatRoom chatRoom, ChatRoomMember chatRoomMember, List<ChatFile> chatFiles,
		ChatMessageType chatMessageType) {
		return ChatMessage.builder()
			.chatRoom(chatRoom)
			.chatRoomMember(chatRoomMember)
			.content(chatMessageType.getDisplayText())
			.messageType(chatMessageType)
			.sendAt(LocalDateTime.now())
			.isVisibleToOpponent(true)
			.images(chatFiles)
			.build();
	}

	public List<ChatFile> getImages() {
		return images.stream()
			.filter(chatImage -> !chatImage.getIsDeleted())
			.sorted(Comparator.comparing(ChatFile::getSortOrder))
			.toList();
	}

	public boolean isMessageTypeText() {
		return this.messageType.equals(ChatMessageType.TEXT);
	}

	public boolean isMessageTypeFile() {
		return this.messageType.equals(ChatMessageType.DOCUMENT) ||
			this.messageType.equals(ChatMessageType.IMAGE);
	}

	public void delete() {
		this.isDeleted = true;
	}

	public void hideFromOpponent() {
		this.isVisibleToOpponent = false;
	}

	public boolean isMessageEstimateType() {
		return this.messageType == ChatMessageType.ESTIMATE ||
			this.messageType == ChatMessageType.ESTIMATE_ACCEPTED ||
			this.messageType == ChatMessageType.ESTIMATE_CANCELED ||
			this.messageType == ChatMessageType.ESTIMATE_UPDATED ||
			this.messageType == ChatMessageType.ESTIMATE_COMPLETED_BY_DIRECTOR ||
			this.messageType == ChatMessageType.ESTIMATE_COMPLETED_BY_MEMBER;
	}

	public boolean isMessageReviewType() {
		return this.messageType == ChatMessageType.REVIEW_COMPLETED;
	}
}
