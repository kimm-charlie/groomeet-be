package com.motd.be.module.member.director_service.controller;

import static com.motd.be.common.constants.Constants.*;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.director_service.dto.response.DirectorServiceFindAllResponse;
import com.motd.be.module.member.director_service.facade.DirectorServiceFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DirectorServiceController {

	private final DirectorServiceFacade directorServiceFacade;

	@GetMapping("/director-services")
	public ResponseEntity<List<DirectorServiceFindAllResponse>> findAll(
		@RequestParam(value = DIRECTOR_SERVICE_PARENT_ID, required = false) Long parentId) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(directorServiceFacade.findAll(parentId));
	}

}
