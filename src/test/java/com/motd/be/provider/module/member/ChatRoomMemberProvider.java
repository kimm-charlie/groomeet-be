package com.motd.be.provider.module.member;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.chat_room_member.repository.ChatRoomMemberRepository;
import com.motd.be.module.member.member.entity.Member;

@Component
public class ChatRoomMemberProvider {

	@Autowired
	private ChatRoomMemberRepository chatRoomMemberRepository;

	public List<ChatRoomMember> findAll() {
		return chatRoomMemberRepository.findAll();
	}

	public ChatRoomMember saveMember(ChatRoom chatRoom, Member member) {
		return chatRoomMemberRepository.save(ChatRoomMember.builder()
			.chatRoom(chatRoom)
			.member(member)
			.build());
	}

	public ChatRoomMember saveDirector(ChatRoom chatRoom, Member director) {
		return chatRoomMemberRepository.save(ChatRoomMember.builder()
			.chatRoom(chatRoom)
			.member(director)
			.isDirector(Boolean.TRUE)
			.build());
	}

	public void deleteAll() {
		chatRoomMemberRepository.deleteAll();
	}

	public ChatRoomMember findById(Long chatRoomMember) {
		return chatRoomMemberRepository.findById(chatRoomMember).orElseThrow();
	}

	public ChatRoomMember saveMemberWithRoomDeletedTrue(ChatRoom chatRoom, Member requester) {
		return chatRoomMemberRepository.save(ChatRoomMember.builder()
			.chatRoom(chatRoom)
			.member(requester)
			.isChatRoomDeleted(Boolean.TRUE)
			.build());
	}

	public ChatRoomMember saveDirectorWithRoomDeletedTrue(ChatRoom chatRoom, Member requester) {
		return chatRoomMemberRepository.save(ChatRoomMember.builder()
			.chatRoom(chatRoom)
			.member(requester)
			.isDirector(Boolean.TRUE)
			.isChatRoomDeleted(Boolean.TRUE)
			.build());
	}
}
