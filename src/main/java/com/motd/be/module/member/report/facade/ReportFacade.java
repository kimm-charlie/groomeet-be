package com.motd.be.module.member.report.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room.processor.ChatRoomProcessor;
import com.motd.be.module.member.chat_room.service.ChatRoomQueryService;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.member_block.service.MemberBlockService;
import com.motd.be.module.member.member_director_favorite.service.MemberDirectorFavoriteService;
import com.motd.be.module.member.report.dto.request.ReportRequest;
import com.motd.be.module.member.report.entity.Report;
import com.motd.be.module.member.report.service.ReportService;
import com.motd.be.module.member.report_file.service.ReportFileService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportFacade {

	private final ReportService reportService;
	private final MemberBlockService memberBlockService;
	private final MemberQueryService memberQueryService;
	private final ChatRoomQueryService chatRoomQueryService;
	private final ChatRoomProcessor chatRoomLeaveProcessor;
	private final MemberDirectorFavoriteService memberDirectorFavoriteService;
	private final ReportFileService reportFileService;

	@Transactional
	public void save(Long reporterId, ReportRequest request) {
		// 1. 신고자 조회
		Member reporter = memberQueryService.findByIdWithLock(reporterId);

		// 2. 신고 대상 조회
		Member reported = memberQueryService.findById(request.getReportedId());

		// 3. 신고 저장
		Report report = reportService.save(reporter, reported, request);

		// 4. 파일 매핑
		reportFileService.mapImagesToReport(report, request.getImageIds(), reporter);

		// 즐겨찾기가 되어있다면 즐겨찾기 해제
		memberDirectorFavoriteService.deleteIfExist(reporter, reported.getId());

		// 4. 차단
		memberBlockService.save(reporter, reported);

		// 5. 채팅방 조회
		List<ChatRoom> chatRooms = chatRoomQueryService.findAllByMemberInBothRolesWithLock(reporter, reported);

		// 6. 채팅방 나가기 처리
		chatRooms.forEach(chatRoom -> {
			List<ChatRoomMember> chatRoomMembers = chatRoom.getChatRoomMembers();
			chatRoomLeaveProcessor.processLeave(reporter, chatRoomMembers, reporterId);
		});
	}
}
