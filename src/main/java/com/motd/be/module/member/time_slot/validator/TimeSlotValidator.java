package com.motd.be.module.member.time_slot.validator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.TimeSlotException;
import com.motd.be.module.member.time_slot.entity.TimeSlot;

@Component
public class TimeSlotValidator {

	public void validateNotPastDate(LocalDate date) {
		if (date.isBefore(LocalDate.now())) {
			throw new CustomRuntimeException(TimeSlotException.PAST_DATE_NOT_ALLOWED);
		}
	}

	public void validateNotPastDateTime(LocalDateTime dateTime) {
		if (dateTime.isBefore(LocalDateTime.now())) {
			throw new CustomRuntimeException(TimeSlotException.PAST_DATE_TIME_NOT_ALLOWED);
		}
	}

	public void validateTimeSlot(LocalDateTime dateTime) {
		List<LocalTime> validSlots = TimeSlot.getAllSlots().stream()
			.map(TimeSlot::getTime)
			.toList();
		if (!validSlots.contains(dateTime.toLocalTime())) {
			throw new CustomRuntimeException(TimeSlotException.INVALID_WISH_TIME_SLOT);
		}
	}

	public void validateWishTimes(List<LocalDateTime> wishTimes) {
		// 중복 검증
		Set<LocalDateTime> uniqueTimes = new HashSet<>(wishTimes);
		if (uniqueTimes.size() != wishTimes.size()) {
			throw new CustomRuntimeException(TimeSlotException.DUPLICATE_WISH_TIME);
		}

		// 유효한 슬롯인지 검증 (시간 부분만 검증)
		List<LocalTime> validSlots = TimeSlot.getAllSlots().stream()
			.map(TimeSlot::getTime)
			.toList();
		for (LocalDateTime dateTime : wishTimes) {
			if (!validSlots.contains(dateTime.toLocalTime())) {
				throw new CustomRuntimeException(TimeSlotException.INVALID_WISH_TIME_SLOT);
			}
		}
	}
}
