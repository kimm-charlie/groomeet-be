package com.motd.be.shared.hackle.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HackleSuccessResponse {

	private String dispatchId;
	private String message;
}
