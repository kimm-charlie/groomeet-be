package com.motd.be.module.member.consulting_request.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.member.entity.Member;

public interface ConsultingRequestRepository extends JpaRepository<ConsultingRequest, Long> {

	boolean existsByMember(Member member);
}
