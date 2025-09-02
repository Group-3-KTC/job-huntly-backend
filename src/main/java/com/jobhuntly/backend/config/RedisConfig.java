package com.jobhuntly.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class RedisConfig {

    @Bean
    public GenericJackson2JsonRedisSerializer jsonSerializer() {
        ObjectMapper om = new ObjectMapper();
        om.findAndRegisterModules(); // JavaTimeModule, etc.
        return new GenericJackson2JsonRedisSerializer(om);
    }

    // Cấu hình mặc định: serializer, prefix, TTL default
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .computePrefixWith(cacheName -> "jobhuntlyapp::" + cacheName + "::") // rõ ràng: prefix + cacheName + "::"
                .entryTtl(Duration.ofMinutes(15))
                .disableCachingNullValues();
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory cf, RedisCacheConfiguration base) {
        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();

        // Detail:
        perCache.put(JOB_DETAIL,         base.entryTtl(Duration.ofMinutes(10)));
        perCache.put(COMPANY_DETAIL,     base.entryTtl(Duration.ofMinutes(10)));
        perCache.put(JOB_LIST_DEFAULT,    base.entryTtl(Duration.ofMinutes(10)));

        // Dictionaries: rất ít đổi
        perCache.put(DICT_CATEGORIES,    base.entryTtl(Duration.ofHours(12)));
        perCache.put(DICT_LEVELS,        base.entryTtl(Duration.ofHours(12)));
        perCache.put(DICT_WORK_TYPES,    base.entryTtl(Duration.ofHours(12)));
        perCache.put(DICT_SKILLS,        base.entryTtl(Duration.ofHours(6)));
        perCache.put(DICT_LOCATIONS_CITY, base.entryTtl(Duration.ofHours(1)));
        perCache.put(DICT_LOCATIONS_WARDS, base.entryTtl(Duration.ofHours(24)));

        // User scopes
        perCache.put(SAVED_JOBS,        base.entryTtl(Duration.ofMinutes(2)));
        perCache.put(APPLICATIONS_LIST, base.entryTtl(Duration.ofMinutes(2)));

        return RedisCacheManager.builder(cf)
                .cacheDefaults(base)
                .withInitialCacheConfigurations(perCache)
                .transactionAware()
                .build();
    }
}
