package com.motd.be.module.member.member_location_mapping.entity;

import static com.motd.be.common.utils.Utils.*;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.member.entity.Member;

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
@Table(uniqueConstraints = @UniqueConstraint(name = "unique_member_location_mapping", columnNames = {
	"activeUniqueKey"}))
public class MemberLocationMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false, name = "location_id")
	private Location location;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false, name = "member_id")
	private Member member;
	@Column(length = 100, nullable = false)
	private String activeUniqueKey; // member_id + location_id 조합의 고유 키

	@Builder
	public MemberLocationMapping(Location location, Member member, String activeUniqueKey) {
		this.location = location;
		this.member = member;
		this.activeUniqueKey = activeUniqueKey;
	}

	public static MemberLocationMapping of(Member member, Location location) {
		return MemberLocationMapping.builder()
			.member(member)
			.location(location)
			.activeUniqueKey(generateMemberLocationMappingUniqueKey(member, location))
			.build();
	}
}
