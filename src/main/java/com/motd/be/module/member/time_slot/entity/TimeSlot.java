package com.motd.be.module.member.time_slot.entity;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TimeSlot {

	SLOT_09_00(LocalTime.of(9, 0)),
	SLOT_09_30(LocalTime.of(9, 30)),
	SLOT_10_00(LocalTime.of(10, 0)),
	SLOT_10_30(LocalTime.of(10, 30)),
	SLOT_11_00(LocalTime.of(11, 0)),
	SLOT_11_30(LocalTime.of(11, 30)),
	SLOT_12_00(LocalTime.of(12, 0)),
	SLOT_12_30(LocalTime.of(12, 30)),
	SLOT_13_00(LocalTime.of(13, 0)),
	SLOT_13_30(LocalTime.of(13, 30)),
	SLOT_14_00(LocalTime.of(14, 0)),
	SLOT_14_30(LocalTime.of(14, 30)),
	SLOT_15_00(LocalTime.of(15, 0)),
	SLOT_15_30(LocalTime.of(15, 30)),
	SLOT_16_00(LocalTime.of(16, 0)),
	SLOT_16_30(LocalTime.of(16, 30)),
	SLOT_17_00(LocalTime.of(17, 0)),
	SLOT_17_30(LocalTime.of(17, 30)),
	SLOT_18_00(LocalTime.of(18, 0)),
	SLOT_18_30(LocalTime.of(18, 30)),
	SLOT_19_00(LocalTime.of(19, 0)),
	SLOT_19_30(LocalTime.of(19, 30)),
	SLOT_20_00(LocalTime.of(20, 0)),
	SLOT_20_30(LocalTime.of(20, 30));

	private final LocalTime time;

	public static List<TimeSlot> getAllSlots() {
		return Arrays.asList(values());
	}
}
