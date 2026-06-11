package com.motd.be.module.member.location.controller;

import static com.motd.be.common.constants.Constants.*;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.location.dto.response.LocationFindAllResponse;
import com.motd.be.module.member.location.facade.LocationFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LocationController {

	private final LocationFacade locationFacade;

	@GetMapping("/locations")
	public ResponseEntity<List<LocationFindAllResponse>> findAll(
		@RequestParam(value = LOCATION_PARENT_ID, required = false) Long locationParentId) {
		return ResponseEntity.status(HttpStatus.OK).body(locationFacade.findAll(locationParentId));
	}
}
