package com.motd.be.common.scheduler;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.member.facade.MemberFacade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"prod-blue", "prod-green", "dev-green", "dev-blue"})
public class MemberUnbanScheduler {

	private final MemberFacade memberFacade;

	@Scheduled(cron = "${scheduler.member-unban.cron}")
	public void unbanMembers() {
		memberFacade.unbanMembers();
	}
}
