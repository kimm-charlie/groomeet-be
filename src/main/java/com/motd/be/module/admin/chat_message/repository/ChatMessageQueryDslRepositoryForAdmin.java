package com.motd.be.module.admin.chat_message.repository;

import static com.motd.be.module.member.chat_message.entity.QChatMessage.*;
import static com.motd.be.module.member.chat_room_member.entity.QChatRoomMember.*;
import static com.motd.be.module.member.member.entity.QMember.*;
import static com.motd.be.module.member.service_estimate.entity.QServiceEstimate.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatMessageQueryDslRepositoryForAdmin {

	private final JPAQueryFactory query;

	public Slice<ChatMessage> findAllByChatRoomId(Long chatRoomId, Long lastMessageId, Pageable pageable) {

		// 커서 기준 메시지
		ChatMessage cursor = findCursorMessage(lastMessageId);

		List<ChatMessage> results = query
			.selectFrom(chatMessage)
			.join(chatMessage.chatRoomMember, chatRoomMember).fetchJoin()
			.join(chatRoomMember.member, member).fetchJoin()
			.leftJoin(chatMessage.serviceEstimate, serviceEstimate).fetchJoin()
			.where(
				buildChatRoomMessageCondition(chatRoomId, cursor)
			)
			.orderBy(chatMessage.sendAt.desc(), chatMessage.id.desc())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		boolean hasNext = results.size() > pageable.getPageSize();
		if (hasNext) {
			results = results.subList(0, pageable.getPageSize());
		}

		return new SliceImpl<>(results, pageable, hasNext);
	}

	/**
	 * 커서 메시지 조회 (마지막 메시지 기준)
	 */
	private ChatMessage findCursorMessage(Long lastMessageId) {
		if (lastMessageId == null) {
			return null;
		}
		return query
			.selectFrom(chatMessage)
			.where(chatMessage.id.eq(lastMessageId))
			.fetchOne();
	}

	/**
	 * 조건 빌더 생성
	 */
	private BooleanBuilder buildChatRoomMessageCondition(Long chatRoomId, ChatMessage cursor) {
		BooleanBuilder condition = new BooleanBuilder()
			.and(chatMessage.chatRoom.id.eq(chatRoomId));

		if (cursor != null) {
			condition.and(
				chatMessage.sendAt.lt(cursor.getSendAt())
					.or(chatMessage.sendAt.eq(cursor.getSendAt())
						.and(chatMessage.id.lt(cursor.getId())))
			);
		}
		return condition;
	}
}
