package com.motd.be.module.member.director_profile_detail.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.director_profile_detail_file.entity.DirectorProfileDetailFile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class DirectorProfileDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(columnDefinition = "LONGTEXT")
	private String contentJson;
	@CreationTimestamp
	private LocalDateTime createdAt;
	@OneToMany(mappedBy = "directorProfileDetail")
	private List<DirectorProfileDetailFile> files = new ArrayList<>();

	@Builder
	public DirectorProfileDetail(String contentJson) {
		this.contentJson = contentJson;
	}

	public void update(String contentJson) {
		this.contentJson = contentJson;
	}

	public List<DirectorProfileDetailFile> getFiles() {
		return this.files.stream()
			.filter(file -> !file.getIsDeleted())
			.toList();
	}
}
