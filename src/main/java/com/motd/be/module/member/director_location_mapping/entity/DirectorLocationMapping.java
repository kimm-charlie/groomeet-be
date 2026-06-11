package com.motd.be.module.member.director_location_mapping.entity;

import static com.motd.be.common.utils.Utils.*;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.location.entity.Location;

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
@Table(uniqueConstraints = @UniqueConstraint(name = "unique_director_location_mapping", columnNames = {
	"activeUniqueKey"}))
public class DirectorLocationMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false, name = "location_id")
	private Location location;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false, name = "director_info_id")
	private DirectorInfo directorInfo;
	@Column(length = 100, nullable = false)
	private String activeUniqueKey; // directorInfo + location 조합의 고유 키

	@Builder
	public DirectorLocationMapping(Location location, DirectorInfo directorInfo, String activeUniqueKey) {
		this.location = location;
		this.directorInfo = directorInfo;
		this.activeUniqueKey = activeUniqueKey;
	}

	public static DirectorLocationMapping of(DirectorInfo directorInfo, Location location) {
		return DirectorLocationMapping.builder()
			.directorInfo(directorInfo)
			.location(location)
			.activeUniqueKey(generateDirectorInfoLocationMappingUniqueKey(directorInfo, location))
			.build();
	}
}
