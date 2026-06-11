package com.motd.be.module.director.consulting_request.dto.response;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_request_file.enums.ConsultingRequestImageCategory;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConsultingRequestImagesResponseForDirector {

	@JsonProperty("FRONT")
	private List<String> front;
	@JsonProperty("SIDE")
	private List<String> side;
	@JsonProperty("TOP")
	private List<String> top;
	@JsonProperty("ASPIRATION")
	private List<String> aspiration;

	public static ConsultingRequestImagesResponseForDirector from(ConsultingRequest consultingRequest) {
		List<String> front = new ArrayList<>();
		List<String> side = new ArrayList<>();
		List<String> top = new ArrayList<>();
		List<String> aspiration = new ArrayList<>();

		consultingRequest.getFiles().forEach(file -> {
			ConsultingRequestImageCategory category = file.getImageCategory();
			if (category.equals(ConsultingRequestImageCategory.FRONT)) {
				front.add(file.getCdnUrl());
				return;
			}
			if (category.equals(ConsultingRequestImageCategory.SIDE)) {
				side.add(file.getCdnUrl());
				return;
			}
			if (category.equals(ConsultingRequestImageCategory.TOP)) {
				top.add(file.getCdnUrl());
				return;
			}

			aspiration.add(file.getCdnUrl());
		});

		return ConsultingRequestImagesResponseForDirector.builder()
			.front(front)
			.side(side)
			.top(top)
			.aspiration(aspiration)
			.build();
	}
}
