package com.motd.be.module.director.director_profile_detail.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.director_profile_detail.dto.request.DirectorProfileUpdateRequestForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_profile_detail.entity.DirectorProfileDetail;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectorProfileDetailServiceForDirector {

	public DirectorProfileDetail update(DirectorInfo directorInfo, DirectorProfileUpdateRequestForDirector request) {
		DirectorProfileDetail directorProfileDetail = directorInfo.getDirectorProfileDetail();

		directorProfileDetail.update(request.getContentJson());

		return directorProfileDetail;
	}
}
