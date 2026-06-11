package com.motd.be.module.director.consulting_request.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ConsultingRequestFindAllResponseForDirector {

	private Boolean hasNext;
	private long totalCount;
	private List<ConsultingRequestResponseForDirector> consultingRequests;

	public static ConsultingRequestFindAllResponseForDirector of(Slice<ConsultingRequest> consultingRequests,
		long totalCount) {
		return ConsultingRequestFindAllResponseForDirector.builder()
			.hasNext(consultingRequests.hasNext())
			.totalCount(totalCount)
			.consultingRequests(ConsultingRequestResponseForDirector.fromList(consultingRequests.getContent()))
			.build();
	}
}
