package com.motd.be.module.member.time_slot.controller;

import static com.motd.be.common.constants.Constants.*;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.time_slot.dto.response.TimeSlotFindAllResponse;
import com.motd.be.module.member.time_slot.facade.TimeSlotFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TimeSlotController {

	private final TimeSlotFacade timeSlotFacade;

	@GetMapping("/time-slots")
	public ResponseEntity<TimeSlotFindAllResponse> findAll(
		@RequestParam(value = DATE_STR) @DateTimeFormat(pattern = DATE_FORMAT) LocalDate date,
		@RequestParam(value = DIRECTOR_MEMBER_ID, required = false) Long directorMemberId) {
		return ResponseEntity.status(HttpStatus.OK).body(timeSlotFacade.findAll(date, directorMemberId));
	}
}
