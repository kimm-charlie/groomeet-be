package com.motd.be.module.member.member_location_mapping.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_location_mapping.entity.MemberLocationMapping;

public interface MemberLocationMappingRepository extends JpaRepository<MemberLocationMapping, Long> {

	@Query("""
		        SELECT mlm
		        FROM MemberLocationMapping mlm
		        JOIN FETCH mlm.location loc
		        WHERE mlm.member.id = :memberId
		""")
	List<MemberLocationMapping> findAllByMemberIdWithLocation(Long memberId);

	@Modifying
	@Query("""
			DELETE FROM MemberLocationMapping mlm
			WHERE mlm.member = :member
		""")
	void deleteAllByMember(Member member);

	@Modifying
	@Query("""
			DELETE FROM MemberLocationMapping mlm
			WHERE mlm IN :toDelete
		""")
	void deleteAllByMappings(List<MemberLocationMapping> toDelete);
}
