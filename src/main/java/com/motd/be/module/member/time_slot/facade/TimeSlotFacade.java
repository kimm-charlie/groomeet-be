package com.motd.be.module.member.time_slot.facade;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.motd.be.module.member.time_slot.dto.response.TimeSlotFindAllResponse;
import com.motd.be.module.member.time_slot.service.TimeSlotService;
import com.motd.be.module.member.time_slot.validator.TimeSlotValidator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TimeSlotFacade {

	private final TimeSlotService timeSlotService;
	private final TimeSlotValidator timeSlotValidator;

	public TimeSlotFindAllResponse findAll(LocalDate date, Long directorMemberId) {
		timeSlotValidator.validateNotPastDate(date);
		return timeSlotService.findAll(date, directorMemberId);
	}
}
