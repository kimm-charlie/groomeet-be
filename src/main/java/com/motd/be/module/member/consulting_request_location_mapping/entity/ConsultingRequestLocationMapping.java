package com.motd.be.module.member.consulting_request_location_mapping.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.location.entity.Location;

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
public class ConsultingRequestLocationMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "location_id", nullable = false)
	private Location location;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "consulting_request_id", nullable = false)
	private ConsultingRequest consultingRequest;
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@Builder
	public ConsultingRequestLocationMapping(Location location, ConsultingRequest consultingRequest) {
		this.location = location;
		this.consultingRequest = consultingRequest;
	}

	public static ConsultingRequestLocationMapping of(Location location, ConsultingRequest consultingRequest) {
		return ConsultingRequestLocationMapping.builder()
			.location(location)
			.consultingRequest(consultingRequest)
			.build();
	}
}
