package com.motd.be.module.director.director_profile_detail.dto.request;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class DirectorProfileUpdateRequestForDirector {

	private String contentJson;
	private List<Long> fileIds;
}
