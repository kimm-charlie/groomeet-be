package com.motd.be.rest_docs;

import static org.springframework.restdocs.snippet.Attributes.*;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Utils {

	public static Attribute getDateFormat() {
		return key("format").value("yyyy.MM.dd");
	}

	public static Attribute getDateTimeFormat() {
		return key("format").value("yyyy.MM.dd HH:mm");
	}

	public static Attribute getTimeFormat() {
		return key("format").value("HH:mm");
	}

	public static Attribute getPhoneNumberFormat() {
		return key("format").value("000-0000-0000");
	}

	/**
	 * Enum 클래스의 모든 값을 ", " 로 구분된 문자열로 반환하는 format attribute 를 생성합니다.
	 * Enum 에 새로운 값이 추가되어도 REST Docs 를 수정할 필요가 없습니다.
	 *
	 * @param enumClass Enum 클래스
	 * @return format attribute
	 */
	public static <E extends Enum<E>> Attribute enumFormat(
		Class<E> enumClass,
		Function<E, String> mapper
	) {
		String values = Arrays.stream(enumClass.getEnumConstants())
			.map(mapper)
			.collect(Collectors.joining(", "));
		return key("format").value(values);
	}

}
