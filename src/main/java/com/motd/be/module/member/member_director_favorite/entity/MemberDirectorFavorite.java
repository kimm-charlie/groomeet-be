package com.motd.be.module.member.member_director_favorite.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.motd.be.module.member.member.entity.Member;

import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
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
@Table(name = "member_director_favorite", uniqueConstraints = @UniqueConstraint(name = "unique_member_director_favorite",
	columnNames = {"member_id", "target_member_id"}))
public class MemberDirectorFavorite {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "memberId", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Member member;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "target_member_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private Member targetMember;
	@CreationTimestamp
	private LocalDateTime createdAt;

	@Builder
	public MemberDirectorFavorite(Member member, Member targetMember) {
		this.member = member;
		this.targetMember = targetMember;
	}
}
