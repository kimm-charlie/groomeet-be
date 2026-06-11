package com.motd.be.module.director.popup.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.director.popup.dto.response.PopupFindAllResponseForDirector;
import com.motd.be.module.director.popup.facade.PopupFacadeForDirector;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directors")
public class PopupControllerForDirector {

	private final PopupFacadeForDirector popupFacadeForDirector;

	@PreAuthorize("hasAnyRole('DIRECTOR')")
	@GetMapping("/popups")
	public ResponseEntity<PopupFindAllResponseForDirector> findAll() {
		return ResponseEntity.status(HttpStatus.OK).body(popupFacadeForDirector.findAll());
	}
}
