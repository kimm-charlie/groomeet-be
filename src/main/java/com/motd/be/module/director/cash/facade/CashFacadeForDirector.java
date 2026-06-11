package com.motd.be.module.director.cash.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.cash.dto.request.CashUseRequestForDirector;
import com.motd.be.module.director.cash.dto.response.CashFindResponseForDirector;
import com.motd.be.module.director.cash.dto.response.CashProductsResponseForDirector;
import com.motd.be.module.director.cash.service.CashProductServiceForDirector;
import com.motd.be.module.director.cash.service.CashServiceForDirector;
import com.motd.be.module.director.cash_transaction_history.service.CashTransactionHistoryServiceForDirector;
import com.motd.be.module.member.cash.entity.CashUsageType;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room.service.ChatRoomService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CashFacadeForDirector {

	private final CashProductServiceForDirector cashProductServiceForDirector;
	private final CashServiceForDirector cashServiceForDirector;
	private final MemberQueryService memberQueryService;
	private final CashTransactionHistoryServiceForDirector cashTransactionHistoryServiceForDirector;
	private final ChatRoomService chatRoomService;

	public CashProductsResponseForDirector findCashProducts() {
		return CashProductsResponseForDirector.from(cashProductServiceForDirector.getAvailableProducts());
	}

	@Transactional
	public void transactionChatStart(Long memberId, CashUseRequestForDirector request) {
		// 디렉터 조회
		Member member = memberQueryService.findByIdWithDirectorAndLock(memberId);

		// referenceId 검증
		ChatRoom chatRoom = chatRoomService.validateToUseCashForChatStart(member, request.getReferenceId());

		// 캐시 사용 처리
		cashServiceForDirector.processTransaction(member, request.getAmount(), CashUsageType.CHAT_START);

		// 채팅방 상태 업데이트
		chatRoomService.updateChatRoomStatusAfterChatStartPaid(chatRoom);

		// 캐시 사용 내역 기록
		cashTransactionHistoryServiceForDirector.save(member, request, CashUsageType.CHAT_START);
	}

	public CashFindResponseForDirector findCash(Long memberId) {
		// 디렉터 조회
		Member director = memberQueryService.findByIdWithDirector(memberId);

		return CashFindResponseForDirector.from(director);
	}
}
