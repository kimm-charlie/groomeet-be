package com.motd.be.module.member.director_service_mapping.entity;

import static com.motd.be.common.utils.Utils.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
@Table(uniqueConstraints = @UniqueConstraint(name = "unique_director_service_mapping", columnNames = {
	"activeUniqueKey"}))
public class DirectorServiceMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "director_Info_id", nullable = false)
	private DirectorInfo directorInfo;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "director_service_id", nullable = false)
	private DirectorService directorService;
	@CreationTimestamp
	private LocalDateTime createdAt;
	@Column(columnDefinition = "boolean default false")
	private Boolean isDeleted;
	@Column(length = 100, nullable = false)
	private String activeUniqueKey; // directorInfo + service 조합의 고유 키

	@Builder
	public DirectorServiceMapping(DirectorInfo directorInfo, DirectorService directorService,
		Boolean isDeleted, String activeUniqueKey) {
		this.directorInfo = directorInfo;
		this.directorService = directorService;
		this.isDeleted = isDeleted;
		this.activeUniqueKey = activeUniqueKey;
	}

	public static DirectorServiceMapping of(DirectorInfo directorInfo, DirectorService directorService) {
		return DirectorServiceMapping.builder()
			.directorInfo(directorInfo)
			.directorService(directorService)
			.activeUniqueKey(generateDirectorInfoDirectorServiceMappingUniqueKey(directorInfo, directorService))
			.build();
	}

	public void delete() {
		this.isDeleted = true;
	}

	public void restore() {
		this.isDeleted = false;
	}
}
