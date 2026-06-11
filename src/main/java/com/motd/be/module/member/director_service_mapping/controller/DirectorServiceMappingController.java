package com.motd.be.module.member.director_service_mapping.controller;

import static com.motd.be.common.constants.Constants.*;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.director.director_service_mapping.dto.response.DirectorServiceFindAllResponseForDirector;
import com.motd.be.module.member.director_service_mapping.facade.DirectorServiceMappingFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DirectorServiceMappingController {

	private final DirectorServiceMappingFacade directorServiceMappingFacade;

	@GetMapping("/directors/{targetMemberId}/services")
	public ResponseEntity<List<DirectorServiceFindAllResponseForDirector>> findAll(
		@PathVariable(TARGET_MEMBER_ID) Long memberId) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(directorServiceMappingFacade.findAll(memberId));
	}
}
