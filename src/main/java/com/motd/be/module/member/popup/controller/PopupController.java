package com.motd.be.module.member.popup.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.popup.dto.response.PopupFindAllResponse;
import com.motd.be.module.member.popup.facade.PopupFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PopupController {

	private final PopupFacade popupFacade;

	@GetMapping("/popups")
	public ResponseEntity<PopupFindAllResponse> findAll() {
		return ResponseEntity.status(HttpStatus.OK).body(popupFacade.findAll());
	}
}
