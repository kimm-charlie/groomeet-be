package com.motd.be.common.scheduler;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.member.facade.MemberFacade;

@ControllerIntegrationTest
public class MemberUnbanSchedulerTest extends BaseIntegrationTest {

	@Autowired
	private MemberFacade memberFacade;

	@Test
	@DisplayName("제안 수락 완료 후 예약일 기준 1일이 지난 제안들을 디렉터 완료 상태로 자동 변경한다")
	void completeEstimatesAfterScheduleCompleted_Success() {
		// given

		// 회원 생성
		Member member1 = memberProvider.saveMemberWithBanned(SignInPlatform.KAKAO, LocalDate.now());
		Member member2 = memberProvider.saveMemberWithBanned(SignInPlatform.KAKAO, LocalDate.now());
		Member member3 = memberProvider.saveMemberWithBanned(SignInPlatform.KAKAO, LocalDate.now());
		Member member4 = memberProvider.saveMemberWithBanned(SignInPlatform.KAKAO, LocalDate.now());
		Member member5 = memberProvider.saveMemberWithBanned(SignInPlatform.KAKAO, LocalDate.now());
		Member member6 = memberProvider.saveMemberWithBanned(SignInPlatform.KAKAO, LocalDate.now());
		Member member7 = memberProvider.saveMemberWithBanned(SignInPlatform.KAKAO, LocalDate.now());

		entityManager.flush();
		entityManager.clear();

		// when
		memberFacade.unbanMembers();

		entityManager.flush();
		entityManager.clear();

		// then
		List<Member> members = memberProvider.findAll();

		members.forEach(member -> {
			assertThat(member.getIsBanned()).isFalse();
			assertThat(member.getBannedAt()).isNull();
			assertThat(member.getUnbannedAt()).isNull();
		});
	}
}
