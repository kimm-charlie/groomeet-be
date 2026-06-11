package com.motd.be.module.member.time_slot.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.service_estimate.service.ServiceEstimateQueryService;
import com.motd.be.module.member.time_slot.dto.response.TimeSlotFindAllResponse;
import com.motd.be.module.member.time_slot.entity.TimeSlot;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

	private final ServiceEstimateQueryService serviceEstimateQueryService;

	public TimeSlotFindAllResponse findAll(LocalDate date, Long directorMemberId) {
		Set<LocalTime> unavailableTimes = new HashSet<>();

		addPastTimeSlots(unavailableTimes, date);

		if (directorMemberId != null) {
			addDirectorUnavailableTimes(unavailableTimes, directorMemberId, date);
		}

		return TimeSlotFindAllResponse.from(unavailableTimes);
	}

	private void addPastTimeSlots(Set<LocalTime> unavailableTimes, LocalDate date) {
		if (!date.isEqual(LocalDate.now())) {
			return;
		}

		LocalTime now = LocalTime.now();
		TimeSlot.getAllSlots().stream()
			.filter(slot -> slot.getTime().isBefore(now))
			.forEach(slot -> unavailableTimes.add(slot.getTime()));
	}

	private void addDirectorUnavailableTimes(Set<LocalTime> unavailableTimes, Long directorMemberId, LocalDate date) {
		List<LocalDateTime> scheduledTimes = serviceEstimateQueryService.findScheduledAtByDirectorMemberIdAndDate(
			directorMemberId, date);
		scheduledTimes.stream()
			.map(LocalDateTime::toLocalTime)
			.collect(Collectors.toCollection(() -> unavailableTimes));
	}
}
