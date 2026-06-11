package com.motd.be.module.member.member_metadata.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_metadata.entity.DeviceType;
import com.motd.be.module.member.member_metadata.entity.MemberMetadata;

public interface MemberMetadataRepository extends JpaRepository<MemberMetadata, Long> {

	@Query("""
			SELECT mm
			FROM MemberMetadata mm
			WHERE mm.member = :member
		""")
	List<MemberMetadata> findAllByMember(Member member);

	@Query("""
			SELECT mm
			FROM MemberMetadata mm
			WHERE mm.member = :member AND mm.deviceType = :deviceType
		""")
	Optional<MemberMetadata> findByMemberAndDeviceType(@Param("member") Member member,
		@Param("deviceType") DeviceType deviceType);
}
