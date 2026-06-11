package com.motd.be.module.director.director_info.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;

import org.hibernate.validator.constraints.Length;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class DirectorInfoUpdateIntroduceTextRequestForDirector {

	@Length(max = 500, message = INTRODUCE_TEXT_OUT_OF_BOUND)
	private String introduceText;
}

