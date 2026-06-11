package com.motd.be.module.admin.director_service.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.director_service.entity.DirectorService;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DirectorServiceFindAllResponseForAdmin {

	private Integer totalCount;
	private Integer page;
	private Boolean hasNext;
	private List<DirectorServiceResponseForAdmin> directorServices;

	public static DirectorServiceFindAllResponseForAdmin from(Slice<DirectorService> directorServices) {
		return DirectorServiceFindAllResponseForAdmin.builder()
			.page(directorServices.getNumber())
			.hasNext(directorServices.hasNext())
			.directorServices(DirectorServiceResponseForAdmin.fromList(directorServices.getContent()))
			.totalCount(directorServices.getNumberOfElements())
			.build();
	}
}
