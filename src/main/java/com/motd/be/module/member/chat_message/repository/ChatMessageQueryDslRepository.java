package com.motd.be.module.member.chat_message.repository;

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
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatMessageQueryDslRepository {

	private final JPAQueryFactory query;

	public Slice<ChatMessage> findAllByChatRoomId(Long chatRoomId, Long lastMessageId, Pageable pageable,
		Long viewerId) {

		// 커서 기준 메시지
		ChatMessage cursor = findCursorMessage(lastMessageId);

		List<ChatMessage> results = query
			.selectFrom(chatMessage)
			.join(chatMessage.chatRoomMember, chatRoomMember).fetchJoin()
			.join(chatRoomMember.member, member).fetchJoin()
			.leftJoin(chatMessage.serviceEstimate, serviceEstimate).fetchJoin()
			.where(buildChatRoomMessageCondition(chatRoomId, cursor), filterVisibleToMember(viewerId))
			.orderBy(chatMessage.sendAt.desc(), chatMessage.id.desc())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		boolean hasNext = results.size() > pageable.getPageSize();
		if (hasNext) {
			results.remove(results.size() - 1);
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
			.and(chatMessage.chatRoom.id.eq(chatRoomId))
			.and(chatMessage.isDeleted.isFalse());

		if (cursor != null) {
			condition.and(
				chatMessage.sendAt.lt(cursor.getSendAt())
					.or(chatMessage.sendAt.eq(cursor.getSendAt())
						.and(chatMessage.id.lt(cursor.getId())))
			);
		}
		return condition;
	}

	private BooleanExpression filterVisibleToMember(Long viewerId) {
		if (viewerId == null) {
			return null;
		}

		return chatMessage.chatRoomMember.member.id.eq(viewerId)
			.or(chatMessage.isVisibleToOpponent.isTrue());
	}

}
