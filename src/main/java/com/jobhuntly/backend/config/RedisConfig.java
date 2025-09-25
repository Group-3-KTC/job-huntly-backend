package com.jobhuntly.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.jobhuntly.backend.constant.CacheConstant.*;

@Configuration
@EnableCaching
@ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

    @Bean
    public GenericJackson2JsonRedisSerializer jsonSerializer() {
        ObjectMapper om = new ObjectMapper();
        om.findAndRegisterModules();
        return new GenericJackson2JsonRedisSerializer(om);
    }

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .computePrefixWith(cacheName -> "jobhuntlyapp::" + cacheName + "::")
                .entryTtl(Duration.ofMinutes(15))
                .disableCachingNullValues();
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory cf, RedisCacheConfiguration base) {
        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();

        perCache.put(JOB_DETAIL,         base.entryTtl(Duration.ofMinutes(10)));
        perCache.put(COMPANY_DETAIL,     base.entryTtl(Duration.ofMinutes(10)));
        perCache.put(JOB_LIST_DEFAULT,   base.entryTtl(Duration.ofMinutes(10)));

        perCache.put(DICT_CATEGORIES,    base.entryTtl(Duration.ofHours(12)));
        perCache.put(DICT_LEVELS,        base.entryTtl(Duration.ofHours(12)));
        perCache.put(DICT_WORK_TYPES,    base.entryTtl(Duration.ofHours(12)));
        perCache.put(DICT_SKILLS,        base.entryTtl(Duration.ofHours(6)));
        perCache.put(DICT_LOCATIONS_CITY, base.entryTtl(Duration.ofHours(24)));
        perCache.put(DICT_LOCATIONS_WARDS, base.entryTtl(Duration.ofHours(24)));
        perCache.put(DICT_PACKAGES, base.entryTtl(Duration.ofHours(24)));

        perCache.put(SAVED_JOBS,        base.entryTtl(Duration.ofMinutes(2)));
        perCache.put(APPLICATIONS_LIST, base.entryTtl(Duration.ofMinutes(2)));

        // AI match cache 24h
        perCache.put(AI_MATCH, base.entryTtl(Duration.ofHours(24)));
        // Marker bypass 1 lần, TTL ngắn
        perCache.put(AI_MATCH_BYPASS, base.entryTtl(Duration.ofMinutes(2)));

        return RedisCacheManager.builder(cf)
                .cacheDefaults(base)
                .withInitialCacheConfigurations(perCache)
                .transactionAware()
                .build();
    }
}