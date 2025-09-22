package com.jobhuntly.backend.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient(
            @Value("${spring.data.redis.host:localhost}") String host,
            @Value("${spring.data.redis.port:6379}") int port,
            @Value("${spring.data.redis.password:}") String password,
            @Value("${spring.data.redis.ssl:false}") boolean ssl) {
        org.redisson.config.Config cfg = new org.redisson.config.Config();
        String scheme = ssl ? "rediss://" : "redis://";
        var single = cfg.useSingleServer()
                .setAddress(scheme + host + ":" + port);
        if (!password.isBlank()) {
            single.setPassword(password);
        }
        // single.setDatabase(0); // nếu cần chọn DB
        return Redisson.create(cfg);
    }
}
