package com.motd.be.common.config;

import static com.motd.be.common.constants.Constants.*;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {

	@Bean
	public Clock clock() {
		return Clock.system(KST);
	}
}
