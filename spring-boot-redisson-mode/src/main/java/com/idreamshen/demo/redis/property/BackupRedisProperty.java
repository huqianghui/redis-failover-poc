package com.idreamshen.demo.redis.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.redis.backup")
public class BackupRedisProperty extends RedisProperty {
}
