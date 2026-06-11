package com.motd.be.module.admin.consulting_request.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsultingRequestFindAllResponseForAdmin {

	private Integer page;
	private Boolean hasNext;
	private Long totalCount;
	private List<ConsultingRequestSummaryResponseForAdmin> consultingRequests;

	public static ConsultingRequestFindAllResponseForAdmin of(Slice<ConsultingRequest> consultingRequests,
		Long totalCount) {
		return ConsultingRequestFindAllResponseForAdmin.builder()
			.page(consultingRequests.getNumber())
			.hasNext(consultingRequests.hasNext())
			.totalCount(totalCount)
			.consultingRequests(consultingRequests.getContent().stream()
				.map(ConsultingRequestSummaryResponseForAdmin::from)
				.toList())
			.build();
	}
}
