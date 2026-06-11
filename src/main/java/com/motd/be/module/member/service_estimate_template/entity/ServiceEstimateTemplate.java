package com.motd.be.module.member.service_estimate_template.entity;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateFile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class ServiceEstimateTemplate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "director_info_id", nullable = false)
	private DirectorInfo directorInfo;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "director_service_id", nullable = false)
	private DirectorService directorService;
	@Column(nullable = false)
	private Long price;
	@Column(nullable = false, length = 100)
	private String title;
	@Column(nullable = false, length = 100)
	private String content;
	@CreationTimestamp
	private LocalDateTime createdAt;
	@Column(columnDefinition = "boolean default false")
	private Boolean isDeleted;
	@OneToMany(mappedBy = "serviceEstimateTemplate", fetch = FetchType.LAZY)
	private List<ServiceEstimateFile> images;

	@Builder
	public ServiceEstimateTemplate(DirectorInfo directorInfo, DirectorService directorService, Long price, String title,
		String content, Boolean isDeleted) {
		this.directorInfo = directorInfo;
		this.directorService = directorService;
		this.price = price;
		this.title = title;
		this.content = content;
		this.isDeleted = isDeleted;
	}

	public List<ServiceEstimateFile> getImages() {
		return images.stream()
			.filter(serviceEstimateTemplateImage -> !serviceEstimateTemplateImage.getIsDeleted())
			.sorted(Comparator.comparing(ServiceEstimateFile::getSortOrder))
			.toList();
	}

	public void updateInfo(String title, Long price, String content) {
		this.title = title;
		this.price = price;
		this.content = content;
	}

	public void delete() {
		this.isDeleted = true;
	}
}
