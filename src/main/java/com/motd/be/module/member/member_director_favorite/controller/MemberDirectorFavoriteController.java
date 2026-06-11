package com.motd.be.module.member.member_director_favorite.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.member_director_favorite.dto.request.MemberDirectorFavoriteRequest;
import com.motd.be.module.member.member_director_favorite.dto.response.MemberDirectorFavoriteFindAllResponse;
import com.motd.be.module.member.member_director_favorite.facade.MemberDirectorFavoriteFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberDirectorFavoriteController {

	private final MemberDirectorFavoriteFacade memberDirectorFavoriteFacade;

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@PostMapping("/members/favorites")
	public ResponseEntity<Void> save(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated MemberDirectorFavoriteRequest request) {
		memberDirectorFavoriteFacade.save(memberId, request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@DeleteMapping("/members/favorites")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated MemberDirectorFavoriteRequest request) {
		memberDirectorFavoriteFacade.delete(memberId, request);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@GetMapping("/members/favorites")
	public ResponseEntity<MemberDirectorFavoriteFindAllResponse> findAll(@AuthenticationPrincipal Long memberId,
		@RequestParam(value = PAGE, defaultValue = ZERO, required = false) int page) {
		return ResponseEntity.status(HttpStatus.OK).body(memberDirectorFavoriteFacade.findAll(memberId, page));
	}
}
