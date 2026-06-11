package com.motd.be.module.member.service_request_wish_time.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.List;

import com.motd.be.module.member.service_request_wish_time.entity.ServiceRequestWishTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WishTimeResponse {

	private String wishTime;

	public static WishTimeResponse from(ServiceRequestWishTime wishTime) {
		return WishTimeResponse.builder()
			.wishTime(formatToDateString(wishTime.getWishTime()))
			.build();
	}

	public static List<WishTimeResponse> fromList(List<ServiceRequestWishTime> wishTimes) {
		return wishTimes.stream()
			.map(WishTimeResponse::from)
			.toList();
	}
}
