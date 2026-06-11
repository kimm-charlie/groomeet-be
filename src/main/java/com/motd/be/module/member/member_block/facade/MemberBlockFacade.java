package com.motd.be.module.member.member_block.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room.processor.ChatRoomProcessor;
import com.motd.be.module.member.chat_room.service.ChatRoomQueryService;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.member_block.dto.request.MemberBlockRequest;
import com.motd.be.module.member.member_block.dto.response.MemberBlockCheckResponse;
import com.motd.be.module.member.member_block.dto.response.MemberBlockFindAllResponse;
import com.motd.be.module.member.member_block.service.MemberBlockQueryService;
import com.motd.be.module.member.member_block.service.MemberBlockService;
import com.motd.be.module.member.member_director_favorite.service.MemberDirectorFavoriteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberBlockFacade {

	private final MemberBlockService memberBlockService;
	private final MemberQueryService memberQueryService;
	private final MemberBlockQueryService memberBlockQueryService;
	private final ChatRoomProcessor chatRoomLeaveProcessor;
	private final ChatRoomQueryService chatRoomQueryService;
	private final MemberDirectorFavoriteService memberDirectorFavoriteService;

	@Transactional
	public void save(Long blockerId, MemberBlockRequest request) {
		// 회원 조회
		Member blocker = memberQueryService.findByIdWithLock(blockerId);

		// 차단할 회원 조회
		Member target = memberQueryService.findById(request.getBlockedId());

		// 즐겨찾기가 되어있다면 즐겨찾기 해제
		memberDirectorFavoriteService.deleteIfExist(blocker, target.getId());

		// 차단
		memberBlockService.save(blocker, target);

		// 채팅방 조회
		List<ChatRoom> chatRooms = chatRoomQueryService.findAllByMemberInBothRolesWithLock(blocker, target);

		// 채팅방 나가기 처리
		chatRooms.forEach(chatRoom -> {
			List<ChatRoomMember> chatRoomMembers = chatRoom.getChatRoomMembers();
			chatRoomLeaveProcessor.processLeave(blocker, chatRoomMembers, blockerId);
		});
	}

	@Transactional
	public void delete(Long blockerId, MemberBlockRequest request) {
		// 회원 조회
		Member member = memberQueryService.findById(blockerId);

		// 차단 삭제
		memberBlockService.delete(member, request.getBlockedId());
	}

	public MemberBlockFindAllResponse findAll(Long blockerId, int page) {
		// 회원 조회
		Member member = memberQueryService.findById(blockerId);

		// 차단 목록 조회
		return memberBlockService.findAll(member, page);
	}

	public MemberBlockCheckResponse check(Long memberId, Long targetMemberId) {
		return MemberBlockCheckResponse.from(
			memberBlockQueryService.existsByBlockerIdAndBlockedId(memberId, targetMemberId));
	}
}
