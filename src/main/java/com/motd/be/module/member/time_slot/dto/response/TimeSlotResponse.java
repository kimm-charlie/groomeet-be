package com.motd.be.module.member.time_slot.dto.response;

import com.motd.be.common.utils.DateFormatUtils;
import com.motd.be.module.member.time_slot.entity.TimeSlot;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TimeSlotResponse {

	private String time;
	private boolean selectable;

	public static TimeSlotResponse of(TimeSlot timeSlot, boolean selectable) {
		return TimeSlotResponse.builder()
			.time(DateFormatUtils.formatToTimeString(timeSlot.getTime()))
			.selectable(selectable)
			.build();
	}
}
