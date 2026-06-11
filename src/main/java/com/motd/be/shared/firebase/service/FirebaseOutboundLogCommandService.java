package com.motd.be.shared.firebase.service;

import org.springframework.stereotype.Service;

import com.motd.be.shared.firebase.entity.FirebaseOutboundLog;
import com.motd.be.shared.firebase.repository.FirebaseOutboundLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FirebaseOutboundLogCommandService {

	private final FirebaseOutboundLogRepository firebaseOutboundLogRepository;

	public void save(FirebaseOutboundLog entity) {
		firebaseOutboundLogRepository.save(entity);
	}
}
