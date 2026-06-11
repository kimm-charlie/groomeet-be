package com.motd.be.module.member.member_block.service;

import static com.motd.be.common.constants.PageSizeConstants.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.MemberBlockException;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_block.dto.response.MemberBlockFindAllResponse;
import com.motd.be.module.member.member_block.entity.MemberBlock;
import com.motd.be.module.member.service_estimate.validator.ServiceEstimateValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberBlockService {

	private final MemberBlockQueryService memberBlockQueryService;
	private final MemberBlockCommandService memberBlockCommandService;
	private final ServiceEstimateValidator serviceEstimateValidator;

	public void save(Member member, Member target) {
		// 1. 이미 차단된 상태인지 확인
		if (memberBlockQueryService.existsByBlockerOrBlocked(member, target)) {
			throw new CustomRuntimeException(MemberBlockException.ALREADY_BLOCKED);
		}

		// 2. 자기 자신 차단 불가
		if (member.getId().equals(target.getId())) {
			throw new CustomRuntimeException(MemberBlockException.CANNOT_BLOCK_SELF);
		}

		// 3. 진행중인 작업이 있는지 확인
		serviceEstimateValidator.validateNoOngoingEstimatesForBlock(member, target);

		// 2. 차단 저장
		try {
			memberBlockCommandService.save(MemberBlock.of(member, target));
		} catch (DataIntegrityViolationException e) {
			throw new CustomRuntimeException(MemberBlockException.ALREADY_BLOCKED);
		}

	}

	public void delete(Member member, Long blockedId) throws CustomRuntimeException {
		// 1. 자기 자신 차단 해제 불가
		if (member.getId().equals(blockedId)) {
			throw new CustomRuntimeException(MemberBlockException.CANNOT_UNBLOCK_SELF);
		}

		// 2. 존재하는지 확인
		if (!memberBlockQueryService.existsByBlockerIdAndBlockedId(member.getId(), blockedId)) {
			throw new CustomRuntimeException(MemberBlockException.ALREADY_DELETED);
		}

		// 2. 차단 삭제
		memberBlockCommandService.deleteByBlockerIdAndBlockedId(member.getId(), blockedId);
	}

	public MemberBlockFindAllResponse findAll(Member member, int page) {
		Pageable pageable = PageRequest.of(page, MEMBER_BLOCK_FIND_ALL_SIZE);

		return MemberBlockFindAllResponse.from(memberBlockQueryService.findAllByBlocker(member, pageable));
	}
}
