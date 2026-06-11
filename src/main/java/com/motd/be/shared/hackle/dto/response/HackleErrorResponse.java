package com.motd.be.shared.hackle.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HackleErrorResponse {

	private String code;
	private String message;
}
