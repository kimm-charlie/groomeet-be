package com.motd.be.module.member.service_request_wish_time.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request_wish_time.entity.ServiceRequestWishTime;

public interface ServiceRequestWishTimeRepository extends JpaRepository<ServiceRequestWishTime, Long> {

	@Query("""
		SELECT wt
		FROM ServiceRequestWishTime wt
		WHERE wt.serviceRequest = :serviceRequest
	""")
	List<ServiceRequestWishTime> findAllByServiceRequest(ServiceRequest serviceRequest);
}
