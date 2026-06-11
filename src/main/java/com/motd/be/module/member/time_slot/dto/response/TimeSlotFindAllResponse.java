package com.motd.be.module.member.time_slot.dto.response;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import com.motd.be.module.member.time_slot.entity.TimeSlot;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TimeSlotFindAllResponse {

	private List<TimeSlotResponse> slots;

	public static TimeSlotFindAllResponse from(Set<LocalTime> unavailableTimes) {
		List<TimeSlotResponse> slots = TimeSlot.getAllSlots().stream()
			.map(slot -> TimeSlotResponse.of(slot, !unavailableTimes.contains(slot.getTime())))
			.toList();
		return TimeSlotFindAllResponse.builder()
			.slots(slots)
			.build();
	}
}
