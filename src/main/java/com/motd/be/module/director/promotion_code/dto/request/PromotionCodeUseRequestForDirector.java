package com.motd.be.module.director.promotion_code.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionCodeUseRequestForDirector {

	@Size(max = 100)
	@NotBlank(message = PROMOTION_CODE_REQUIRED)
	private String promotionCode;
}
