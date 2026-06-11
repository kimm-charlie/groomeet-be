package com.motd.be.module.admin.director_info.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.admin.director_info.repository.DirectorInfoRepositoryForAdmin;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectorQueryServiceForAdmin {

	private final DirectorInfoRepositoryForAdmin directorInfoRepositoryForAdmin;

	public long countDirectors() {
		return directorInfoRepositoryForAdmin.countByMemberIsWithdrawalFalse();
	}

	public long countTodayDirectors(LocalDateTime startOfDay, LocalDateTime endOfDay) {
		return directorInfoRepositoryForAdmin.countByCreatedAtBetweenAndMemberIsWithdrawalFalse(startOfDay,
			endOfDay);
	}
}
