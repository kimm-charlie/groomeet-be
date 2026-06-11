package com.motd.be.module.member.chat_room.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ChatRoomMemberException;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class ChatRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;
	@Column(nullable = false, columnDefinition = "boolean default false")
	private Boolean isDeleted;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_message_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private ChatMessage lastMessage;
	@OneToMany(mappedBy = "chatRoom", fetch = FetchType.LAZY)
	private List<ChatRoomMember> chatRoomMembers;
	@OneToMany(mappedBy = "chatRoom", fetch = FetchType.LAZY)
	private List<ChatMessage> messages;
	@OneToMany(mappedBy = "chatRoom", fetch = FetchType.LAZY)
	private List<ChatRoomServiceEstimateMapping> chatRoomServiceEstimateMappings;
	@Column(columnDefinition = "boolean default false")
	private Boolean isDirectorPaid;
	private LocalDateTime directorPaidAt;

	@Builder
	public ChatRoom(Boolean isDeleted, ChatMessage lastMessage, Boolean isDirectorPaid, LocalDateTime directorPaidAt) {
		this.isDeleted = isDeleted;
		this.lastMessage = lastMessage;
		this.isDirectorPaid = isDirectorPaid;
		this.directorPaidAt = directorPaidAt;
	}

	public Member getOtherMember(Member member) {
		return this.chatRoomMembers.stream()
			.map(ChatRoomMember::getMember)
			.filter(m -> !m.getId().equals(member.getId()))
			.findFirst()
			.orElseThrow();
	}

	public ChatRoomMember getChatRoomMember(Member member) {
		return this.chatRoomMembers.stream()
			.filter(cm -> cm.getMember().getId().equals(member.getId()))
			.findFirst()
			.orElseThrow(() -> new CustomRuntimeException(ChatRoomMemberException.NOT_IN_CHAT_ROOM));
	}

	public ServiceEstimate getLatestEstimate() {
		return this.chatRoomServiceEstimateMappings.stream()
			.map(ChatRoomServiceEstimateMapping::getServiceEstimate)
			.max(Comparator.comparing(ServiceEstimate::getCreatedAt))
			.orElseThrow();
	}

	public void updateLastMessage(ChatMessage lastMessage) {
		this.lastMessage = lastMessage;
	}

	public void addChatRoomMember(List<ChatRoomMember> chatRoomMembers) {
		this.chatRoomMembers = chatRoomMembers;
	}

	public void updateChatRoomStatusAfterChatStartPaid() {
		this.isDirectorPaid = true;
		this.directorPaidAt = LocalDateTime.now();
	}

	/**
	 * 디렉터 결제 여부
	 * true 인경우 -> 디렉터가 결제를 했거나, 디렉터가 무료 체험 기간인 경우
	 *
	 * @return
	 */
	public boolean isDirectorPaid() {
		// 1. 이미 유료면 바로 true
		if (isDirectorPaid) {
			return true;
		}

		// 2. director 조회
		Member director = chatRoomMembers.stream()
			.filter(ChatRoomMember::getIsDirector)
			.map(ChatRoomMember::getMember)
			.findFirst()
			.orElseThrow();

		// 3. free 기간 체크 (종료일 포함)
		LocalDate freeUntil = director.getDirectorInfo().getOnboardingPassEndsAt();
		LocalDate today = LocalDate.now();

		// 무료 종료일이 있고, 오늘이 종료일을 지나지 않았으면 무료
		return freeUntil != null && !today.isAfter(freeUntil);
	}
}
