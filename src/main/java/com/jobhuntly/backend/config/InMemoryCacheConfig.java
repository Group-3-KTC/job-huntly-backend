package com.jobhuntly.backend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.jobhuntly.backend.constant.CacheConstant.*;

@Configuration
@EnableCaching
@ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "false")
public class InMemoryCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // Dùng cache in-memory, giữ nguyên tên cache để ứng dụng hoạt động như cũ
        return new ConcurrentMapCacheManager(
                JOB_DETAIL,
                COMPANY_DETAIL,
                JOB_LIST_DEFAULT,
                DICT_CATEGORIES,
                DICT_LEVELS,
                DICT_WORK_TYPES,
                DICT_SKILLS,
                DICT_LOCATIONS_CITY,
                DICT_LOCATIONS_WARDS,
                SAVED_JOBS,
                APPLICATIONS_LIST,
                AI_MATCH,
                AI_MATCH_BYPASS
        );
    }
}