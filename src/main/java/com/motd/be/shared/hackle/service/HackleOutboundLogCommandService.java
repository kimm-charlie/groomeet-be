package com.motd.be.shared.hackle.service;

import org.springframework.stereotype.Service;

import com.motd.be.shared.hackle.entity.HackleOutboundLog;
import com.motd.be.shared.hackle.repository.HackleOutboundLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HackleOutboundLogCommandService {

	private final HackleOutboundLogRepository hackleOutboundLogRepository;

	public void save(HackleOutboundLog log) {
		hackleOutboundLogRepository.save(log);
	}
}
