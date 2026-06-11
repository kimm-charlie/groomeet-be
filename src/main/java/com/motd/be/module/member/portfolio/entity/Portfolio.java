package com.motd.be.module.member.portfolio.entity;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;

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
public class Portfolio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "director_service_id", nullable = false)
	private DirectorService directorService;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "director_info_id", nullable = false)
	private DirectorInfo directorInfo;
	@Column(length = 100, nullable = false)
	private String title;
	@Column(length = 1000)
	private String content;
	@CreationTimestamp
	private LocalDateTime createdAt;
	@Column(columnDefinition = "boolean default false")
	private Boolean isDeleted;
	@Column(nullable = false)
	private Long price;
	@Column(columnDefinition = "boolean default false")
	private Boolean isPopular;
	private LocalDateTime popularAt;
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "portfolio")
	private List<PortfolioFile> files;

	@Builder
	public Portfolio(DirectorService directorService, DirectorInfo directorInfo, String title, String content,
		Boolean isDeleted, Long price) {
		this.directorService = directorService;
		this.directorInfo = directorInfo;
		this.title = title;
		this.content = content;
		this.isDeleted = isDeleted;
		this.price = price;
	}

	public boolean isOwnedBy(DirectorInfo directorInfo) {
		return this.directorInfo.getId().equals(directorInfo.getId());
	}

	public void delete() {
		this.isDeleted = true;
	}

	public void update(String title, String content, DirectorService directorService, Long price) {
		this.title = title;
		this.content = content;
		this.directorService = directorService;
		this.price = price;
	}

	public void markAsPopular() {
		this.isPopular = true;
		this.popularAt = LocalDateTime.now();
	}

	public void unmarkAsPopular() {
		this.isPopular = false;
		this.popularAt = null;
	}

	public List<PortfolioFile> getFiles() {
		return files.stream()
			.filter(image -> !image.getIsDeleted())
			.sorted(Comparator.comparing(PortfolioFile::getSortOrder))
			.toList();
	}
}
