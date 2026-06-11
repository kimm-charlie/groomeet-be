package com.motd.be.module.member.review.entity;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.review_file.entity.ReviewFile;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(uniqueConstraints = @UniqueConstraint(name = "unique_review_per_estimate", columnNames = {
	"service_estimate_id", "writer_id"
}))
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_estimate_id", nullable = false,
		foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private ServiceEstimate serviceEstimate;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "writer_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Member writer;
	@Column(nullable = false, length = 255)
	private String title;
	@Column(nullable = false, length = 1000)
	private String content;
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;
	@Column(columnDefinition = "boolean default false")
	private Boolean isDeleted;
	@OneToMany(mappedBy = "review", fetch = FetchType.LAZY)
	private List<ReviewFile> images;

	@Builder
	public Review(ServiceEstimate serviceEstimate, Member writer, String title, String content, Boolean isDeleted) {
		this.serviceEstimate = serviceEstimate;
		this.writer = writer;
		this.title = title;
		this.content = content;
		this.isDeleted = isDeleted;
	}

	public List<ReviewFile> getImages() {
		if (images == null) {
			return List.of();
		}
		return images.stream()
			.filter(reviewImage -> !reviewImage.getIsDeleted())
			.sorted(Comparator.comparing(ReviewFile::getSortOrder))
			.toList();
	}

	public void update(String content) {
		this.content = content;
	}

	public void delete() {
		this.isDeleted = true;
	}

	public void setImage(List<ReviewFile> images) {
		this.images = images;
	}

	public boolean isReviewEditable(Long requestedMemberId) {
		if (requestedMemberId == null) {
			return false;
		}

		return this.serviceEstimate.getMemberCompletedAt() != null && requestedMemberId.equals(writer.getId()) &&
			!this.serviceEstimate.getMemberCompletedAt().isBefore(LocalDateTime.now().minusDays(7));
	}
}
