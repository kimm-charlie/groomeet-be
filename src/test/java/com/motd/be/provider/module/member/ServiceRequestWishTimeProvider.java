package com.motd.be.provider.module.member;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request_wish_time.entity.ServiceRequestWishTime;
import com.motd.be.module.member.service_request_wish_time.repository.ServiceRequestWishTimeRepository;

@Component
public class ServiceRequestWishTimeProvider {

	@Autowired
	private ServiceRequestWishTimeRepository serviceRequestWishTimeRepository;

	public ServiceRequestWishTime save(ServiceRequest serviceRequest, LocalDateTime wishTime) {
		return serviceRequestWishTimeRepository.save(ServiceRequestWishTime.of(serviceRequest, wishTime));
	}

	public List<ServiceRequestWishTime> saveAll(ServiceRequest serviceRequest, List<LocalDateTime> wishTimes) {
		List<ServiceRequestWishTime> entities = wishTimes.stream()
			.map(time -> ServiceRequestWishTime.of(serviceRequest, time))
			.toList();
		return serviceRequestWishTimeRepository.saveAll(entities);
	}

	public List<ServiceRequestWishTime> findAllByServiceRequest(ServiceRequest serviceRequest) {
		return serviceRequestWishTimeRepository.findAllByServiceRequest(serviceRequest);
	}
}
