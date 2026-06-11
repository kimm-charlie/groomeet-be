package com.motd.be.module.director.member.service;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.DirectorInfoException;
import com.motd.be.exception.exceptions.MemberException;
import com.motd.be.module.director.member.repository.MemberRepositoryForDirector;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberQueryServiceForDirector {

	private final MemberRepositoryForDirector memberRepositoryForDirector;

	public Member findById(Long memberId) {
		return memberRepositoryForDirector.findById(memberId)
			.orElseThrow(() -> new CustomRuntimeException(MemberException.NOT_FOUND));
	}

	public Member findByIdWithDirector(Long memberId) {
		Member member = memberRepositoryForDirector.findByIdWithDirector(memberId)
			.orElseThrow(() -> new CustomRuntimeException(MemberException.NOT_FOUND));

		if (member.getDirectorInfo() == null || !member.isDirector()) {
			throw new CustomRuntimeException(DirectorInfoException.DIRECTOR_INFO_NOT_FOUND);
		}
		return member;
	}
}
