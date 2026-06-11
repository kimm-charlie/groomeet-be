package com.motd.be.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableAsync
public class TaskExecutorConfig {

	/**
	 * ===============================
	 * Async Executor
	 * - 외부 API 호출
	 * - Push 발송
	 * - 이벤트 트리거
	 * ===============================
	 */
	@Bean(name = "asyncTaskExecutor")
	public ThreadPoolTaskExecutor asyncTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(4);        // CPU * 2
		executor.setMaxPoolSize(8);         // CPU * 4
		executor.setQueueCapacity(200);     // burst 흡수
		executor.setThreadNamePrefix("async-");
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(30);
		executor.initialize();
		return executor;
	}

	/**
	 * ===============================
	 * WebSocket / SSE 전용 Scheduler
	 * - heartbeat
	 * - keep-alive
	 * ===============================
	 */
	@Bean(name = "wsTaskScheduler")
	public ThreadPoolTaskScheduler wsTaskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(1);           // 직렬 처리 의도 OK
		scheduler.setThreadNamePrefix("ws-heartbeat-");
		scheduler.setWaitForTasksToCompleteOnShutdown(true);
		scheduler.setAwaitTerminationSeconds(10);
		scheduler.initialize();
		return scheduler;
	}

}
