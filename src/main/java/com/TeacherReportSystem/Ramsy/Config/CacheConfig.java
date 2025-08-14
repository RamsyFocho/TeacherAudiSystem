package com.TeacherReportSystem.Ramsy.Config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String REPORTS_CACHE = "reports";
    public static final String REPORT_STATS_CACHE = "reportStats";
    public static final String TEACHER_REPORTS_CACHE = "teacherReports";
    public static final String ESTABLISHMENT_REPORTS_CACHE = "establishmentReports";
    public static final String SANCTIONS_CACHE = "sanctions";
    public static final String REFERENCE_DATA_CACHE = "referenceData";

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES) // Default TTL
                .maximumSize(1000); // Maximum number of entries in the cache
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                REPORTS_CACHE,
                REPORT_STATS_CACHE,
                TEACHER_REPORTS_CACHE,
                ESTABLISHMENT_REPORTS_CACHE,
                SANCTIONS_CACHE,
                REFERENCE_DATA_CACHE
        );
        
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }
}
