package com.motd.be.module.member.service_request.dto.request;

import com.motd.be.module.member.service_request.entity.StopReceivingEstimateReason;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestUpdateReceivingEstimateRequest {

	@NotNull(message = "제안 수신 상태 변경 사유는 필수입니다.")
	private StopReceivingEstimateReason reason;
}
