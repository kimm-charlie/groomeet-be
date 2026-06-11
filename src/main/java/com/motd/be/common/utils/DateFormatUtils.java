package com.motd.be.common.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateFormatUtils {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
	private static final DateTimeFormatter DATE_FORMATTER_FOR_MOBILE_OK = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter authenticatedAtFormatter = DateTimeFormatter.ofPattern(
		"yyyy-MM-dd HH:mm:ss.SSS");

	// LocalDate을 "yyyy.MM.dd" 형식의 문자열로 변환
	public static String formatToDateString(LocalDate date) {
		return date != null ? date.format(DATE_FORMATTER) : null;
	}

	// LocalDateTime을 "yyyy.MM.dd HH:mm" 형식의 문자열로 변환
	public static String formatToDateString(LocalDateTime dateTime) {
		return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
	}

	// "yyyy.MM.dd HH:mm" 형식의 문자열을 LocalDateTime으로 변환
	public static LocalDateTime parseToLocalDateTime(String dateString) {
		return dateString != null ? LocalDateTime.parse(dateString, DATE_TIME_FORMATTER) : null;
	}

	public static LocalDate parseToLocalDateFromMobileOk(String dateString) {
		return LocalDate.parse(dateString, DATE_FORMATTER_FOR_MOBILE_OK);
	}

	public static LocalDateTime parseToLocalDateTimeFromAuthenticatedAt(String dateString) {
		return LocalDateTime.parse(dateString, authenticatedAtFormatter);
	}

	// LocalTime을 "HH:mm" 형식의 문자열로 변환
	public static String formatToTimeString(LocalTime time) {
		return time != null ? time.format(TIME_FORMATTER) : null;
	}
}
