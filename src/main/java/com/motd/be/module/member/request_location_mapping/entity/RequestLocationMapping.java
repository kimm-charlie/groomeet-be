package com.motd.be.module.member.request_location_mapping.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class RequestLocationMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "location_id", nullable = false)
	private Location location;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_request_id", nullable = false)
	private ServiceRequest serviceRequest;

	@Builder
	public RequestLocationMapping(Location location, ServiceRequest serviceRequest) {
		this.location = location;
		this.serviceRequest = serviceRequest;
	}

	public static RequestLocationMapping of(Location location, ServiceRequest serviceRequest) {
		return RequestLocationMapping.builder()
			.location(location)
			.serviceRequest(serviceRequest)
			.build();
	}
}
