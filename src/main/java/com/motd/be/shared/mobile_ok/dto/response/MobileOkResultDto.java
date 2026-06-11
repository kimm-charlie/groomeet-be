package com.motd.be.shared.mobile_ok.dto.response;

import com.dreamsecurity.json.JSONObject;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MobileOkResultDto {

	private String clientTxId;
	private String userName;
	private String userPhone;
	private String userBirthday;
	private String userGender;
	private String issueDate;
	private String ci;
	private String di;
	private String userNation;

	public static MobileOkResultDto from(JSONObject json) {
		MobileOkResultDto dto = new MobileOkResultDto();

		dto.clientTxId = json.optString("clientTxId", null);
		dto.userName = json.optString("userName", null);
		dto.userPhone = json.optString("userPhone", null);
		dto.userBirthday = json.optString("userBirthday", null);
		dto.userGender = json.optString("userGender", null);
		dto.issueDate = json.optString("issueDate", null);
		dto.ci = json.optString("ci", null);
		dto.di = json.optString("di", null);
		dto.userNation = json.optString("userNation", null);

		return dto;
	}
}
