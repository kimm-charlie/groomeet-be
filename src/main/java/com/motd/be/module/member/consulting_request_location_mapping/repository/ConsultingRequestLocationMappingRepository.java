package com.motd.be.module.member.consulting_request_location_mapping.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motd.be.module.member.consulting_request_location_mapping.entity.ConsultingRequestLocationMapping;

public interface ConsultingRequestLocationMappingRepository extends JpaRepository<ConsultingRequestLocationMapping, Long> {
}
