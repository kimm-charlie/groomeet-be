package com.motd.be.module.member.chat_room_member.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class ChatRoomMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false, name = "chat_room_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private ChatRoom chatRoom;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false, name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Member member;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "last_read_message_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private ChatMessage lastReadMessage;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "last_visible_message_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private ChatMessage lastVisibleMessage;
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isDirector;
	private LocalDateTime leftAt;
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isChatRoomDeleted;

	@Builder
	public ChatRoomMember(ChatRoom chatRoom, Member member, ChatMessage lastReadMessage,
		ChatMessage lastVisibleMessage, Boolean isDirector, LocalDateTime leftAt, Boolean isChatRoomDeleted) {
		this.chatRoom = chatRoom;
		this.member = member;
		this.lastReadMessage = lastReadMessage;
		this.lastVisibleMessage = lastVisibleMessage;
		this.isDirector = isDirector;
		this.leftAt = leftAt;
		this.isChatRoomDeleted = isChatRoomDeleted;
	}

	public void updateLastReadMessage(ChatMessage lastReadMessage) {
		this.lastReadMessage = lastReadMessage;
	}

	public void updateToDeleteChatRoom() {
		this.isChatRoomDeleted = true;
	}

	public void updateLastVisibleMessage(ChatMessage lastVisibleMessage) {
		this.lastVisibleMessage = lastVisibleMessage;
	}

	public void delete() {
		this.isChatRoomDeleted = true;
	}

	public void recoverFromLeftChatRoom() {
		this.isChatRoomDeleted = false;
	}
}
