package com.motd.be.module.director.member.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.director.member.dto.response.MemberProfileSummaryResponseForDirector;
import com.motd.be.module.director.member.facade.MemberFacadeForDirector;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directors")
public class MemberControllerForDirector {

	private final MemberFacadeForDirector memberFacade;

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/members/{targetMemberId}/profile-summary")
	public ResponseEntity<MemberProfileSummaryResponseForDirector> findProfileSummary(
		@PathVariable(TARGET_MEMBER_ID) Long targetMemberId) {
		return ResponseEntity.status(HttpStatus.OK).body(memberFacade.findProfileSummary(targetMemberId));
	}
}
