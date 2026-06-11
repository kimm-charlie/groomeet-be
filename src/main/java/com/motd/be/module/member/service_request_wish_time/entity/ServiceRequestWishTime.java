package com.motd.be.module.member.service_request_wish_time.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.service_request.entity.ServiceRequest;

import jakarta.persistence.Column;
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
public class ServiceRequestWishTime {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_request_id", nullable = false)
	private ServiceRequest serviceRequest;
	@Column(nullable = false)
	private LocalDateTime wishTime;
	@Column(columnDefinition = "boolean default false")
	private Boolean isConfirmed;

	@Builder
	public ServiceRequestWishTime(ServiceRequest serviceRequest, LocalDateTime wishTime) {
		this.serviceRequest = serviceRequest;
		this.wishTime = wishTime;
	}

	public static ServiceRequestWishTime of(ServiceRequest serviceRequest, LocalDateTime wishTime) {
		return ServiceRequestWishTime.builder()
			.serviceRequest(serviceRequest)
			.wishTime(wishTime)
			.build();
	}

	public void confirm() {
		this.isConfirmed = true;
	}

	public void unconfirm() {
		this.isConfirmed = false;
	}

	public void updateWishTime(LocalDateTime newWishTime) {
		this.wishTime = newWishTime;
	}
}
