package com.motd.be.module.director.chat_room.repository;

import static com.motd.be.module.member.chat_room.entity.QChatRoom.*;
import static com.motd.be.module.member.chat_room_member.entity.QChatRoomMember.*;
import static com.motd.be.module.member.chat_room_service_estimate_mapping.entity.QChatRoomServiceEstimateMapping.*;
import static com.motd.be.module.member.member.entity.QMember.*;
import static com.motd.be.module.member.service_estimate.entity.QServiceEstimate.*;
import static com.motd.be.module.member.service_request.entity.QServiceRequest.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatRoomQueryDslRepositoryForDirector {

	private final JPAQueryFactory query;

	//todo 이거 만약에 차단된 사용자가 메세지 보면 lastMessage 업데이트 되고 안됨에 따라 뭔가 이상하게 될거같은데
	public Slice<ChatRoom> findAllForDirector(
		Member director,
		Long directorServiceId,
		boolean showOnlyUnread,
		String word,
		String status,
		Pageable pageable
	) {

		JPAQuery<ChatRoom> query = this.query
			.select(chatRoom)
			.from(chatRoom)
			.join(chatRoom.chatRoomMembers, chatRoomMember)
			.where(
				chatRoom.isDeleted.isFalse(),
				chatRoomMember.member.eq(director),
				chatRoomMember.isDirector.isTrue(),
				chatRoomMember.isChatRoomDeleted.isFalse(),
				existsServiceEstimateCondition(directorServiceId, status),
				existsUnreadCondition(showOnlyUnread),
				existsWordCondition(word)
			)
			.orderBy(chatRoomMember.lastVisibleMessage.sendAt.desc(), chatRoomMember.lastVisibleMessage.id.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1);

		List<ChatRoom> results = query.fetch();

		boolean hasNext = results.size() > pageable.getPageSize();
		if (hasNext)
			results.remove(results.size() - 1);

		return new SliceImpl<>(results, pageable, hasNext);
	}

	private BooleanExpression existsServiceEstimateCondition(
		Long directorServiceId,
		String status
	) {
		if (directorServiceId == null && (status == null || status.isBlank())) {
			return null;
		}

		BooleanExpression condition = chatRoomServiceEstimateMapping.chatRoom.eq(chatRoom);

		if (directorServiceId != null) {
			condition = condition.and(
				serviceEstimate.serviceRequest.directorService.id.eq(directorServiceId)
			);
		}

		if (status != null && !status.isBlank()) {
			ServiceEstimateStatus estimateStatus = ServiceEstimateStatus.from(status);

			condition = switch (estimateStatus) {
				case PENDING -> condition.and(serviceEstimate.status.eq(ServiceEstimateStatus.PENDING));

				case ONGOING -> condition.and(
					serviceEstimate.status.in(
						ServiceEstimateStatus.ONGOING,
						ServiceEstimateStatus.DIRECTOR_DONE
					)
				);

				default -> condition;
			};
		}

		return JPAExpressions
			.selectOne()
			.from(chatRoomServiceEstimateMapping)
			.join(chatRoomServiceEstimateMapping.serviceEstimate, serviceEstimate)
			.join(serviceEstimate.serviceRequest, serviceRequest)
			.where(condition)
			.exists();
	}

	private BooleanExpression existsWordCondition(String word) {
		if (word == null || word.isBlank())
			return null;

		return JPAExpressions
			.selectOne()
			.from(chatRoomServiceEstimateMapping)
			.join(chatRoomServiceEstimateMapping.serviceEstimate, serviceEstimate)
			.join(serviceEstimate.serviceRequest, serviceRequest)
			.join(serviceRequest.member, member)
			.where(
				chatRoomServiceEstimateMapping.chatRoom.eq(chatRoom),
				member.nickname.contains(word)
					.or(serviceRequest.directorService.name.contains(word))
			)
			.exists();
	}

	private BooleanExpression existsUnreadCondition(boolean showOnlyUnread) {
		if (!showOnlyUnread)
			return null;

		return chatRoomMember.lastVisibleMessage.id.isNotNull()
			.and(
				chatRoomMember.lastReadMessage.id.isNull()
					.or(chatRoomMember.lastReadMessage.id.lt(chatRoomMember.lastVisibleMessage.id))
			);
	}
}
