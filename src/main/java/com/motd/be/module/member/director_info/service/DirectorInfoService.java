package com.motd.be.module.member.director_info.service;

import static com.motd.be.common.constants.PageSizeConstants.*;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.module.member.director_info.dto.request.DirectorInfoRegisterRequest;
import com.motd.be.module.member.director_info.dto.response.DirectorRankMainViewResponse;
import com.motd.be.module.member.director_info.dto.response.DirectorRankPageResponse;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectorInfoService {

	private final DirectorInfoCommandService directorInfoCommandService;
	private final DirectorInfoQueryService directorInfoQueryService;

	public DirectorInfo createDirectorInfo(DirectorInfoRegisterRequest request) {
		return directorInfoCommandService.save(request.toEntity());
	}

	public DirectorRankMainViewResponse findDirectorRankInMainView() {
		Pageable pageable = PageRequest.of(0, DIRECTOR_RANK_MAIN_VIEW_SIZE);
		Slice<DirectorInfo> directors = directorInfoQueryService.findDirectorRank(pageable);

		return DirectorRankMainViewResponse.from(directors);
	}

	public DirectorRankPageResponse findDirectorRankInRankView(int page) {
		Pageable pageable = PageRequest.of(page, DIRECTOR_RANK_RANK_VIEW_SIZE);
		Slice<DirectorInfo> directors = directorInfoQueryService.findDirectorRank(pageable);

		return DirectorRankPageResponse.from(directors);
	}

	public void incrementReviewCount(DirectorInfo directorInfo) {
		directorInfo.incrementReviewCount();
	}

	public void incrementCompletedEstimateCountBulk(List<ServiceEstimate> serviceEstimates) {
		if (serviceEstimates.isEmpty()) {
			return;
		}
		Map<Long, Integer> directorEstimateCountMap = serviceEstimates.stream()
			.collect(
				java.util.stream.Collectors.groupingBy(
					estimate -> estimate.getDirectorInfo().getId(),
					java.util.stream.Collectors.summingInt(e -> 1)
				)
			);

		directorInfoCommandService.updateCompletedEstimateCountsByMembers(directorEstimateCountMap);
	}

	public void updateIsProfileDetailExistToTrue(Member director) {
		if (director.getDirectorInfo().getIsProfileDetailExist()) {
			return;
		}
		director.getDirectorInfo().updateIsProfileDetailExistToTrue();
	}
}

