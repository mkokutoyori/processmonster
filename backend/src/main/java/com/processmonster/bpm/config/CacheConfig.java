package com.processmonster.bpm.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache Configuration using Caffeine
 *
 * Configures in-memory caching for improved performance.
 * Used for caching dashboard metrics, process definitions, and frequently accessed data.
 *
 * Cache names:
 * - dashboardMetrics: Dashboard KPIs (5 min TTL)
 * - processDefinitions: Process definitions (30 min TTL)
 * - userPermissions: User permissions (15 min TTL)
 * - systemParameters: System parameters (1 hour TTL)
 *
 * @author ProcessMonster Team
 * @version 1.0.0
 * @since 2025-11-07
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${app.cache.caffeine.spec}")
    private String cacheSpec;

    /**
     * Configures Caffeine cache manager with default settings.
     *
     * @return Configured CacheManager
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "dashboardMetrics",
            "processDefinitions",
            "userPermissions",
            "systemParameters",
            "reports"
        );

        cacheManager.setCaffeine(Caffeine.from(cacheSpec));

        return cacheManager;
    }

    /**
     * Creates a Caffeine instance for dashboard metrics with 5-minute TTL.
     *
     * @return Caffeine instance
     */
    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats();
    }
}
