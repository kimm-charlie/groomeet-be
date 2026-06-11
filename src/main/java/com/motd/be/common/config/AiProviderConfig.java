package com.motd.be.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SionicAiProperties.class)
public class AiProviderConfig {
}
