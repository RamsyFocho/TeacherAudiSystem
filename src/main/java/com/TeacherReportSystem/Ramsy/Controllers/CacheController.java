package com.TeacherReportSystem.Ramsy.Controllers;

import com.TeacherReportSystem.Ramsy.Config.CacheConfig;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final CacheManager cacheManager;

    public CacheController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @GetMapping("/stats")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get all cache names
        String[] cacheNames = {
            CacheConfig.REPORTS_CACHE,
            CacheConfig.REPORT_STATS_CACHE,
            CacheConfig.TEACHER_REPORTS_CACHE,
            CacheConfig.ESTABLISHMENT_REPORTS_CACHE,
            CacheConfig.SANCTIONS_CACHE,
            CacheConfig.REFERENCE_DATA_CACHE
        };
        
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null && cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
                @SuppressWarnings("unchecked")
                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = 
                    (com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache.getNativeCache();
                
                CacheStats cacheStats = nativeCache.stats();
                
                Map<String, Object> cacheStatsMap = new HashMap<>();
                cacheStatsMap.put("hitCount", cacheStats.hitCount());
                cacheStatsMap.put("hitRate", cacheStats.hitRate());
                cacheStatsMap.put("missCount", cacheStats.missCount());
                cacheStatsMap.put("missRate", cacheStats.missRate());
                cacheStatsMap.put("loadSuccessCount", cacheStats.loadSuccessCount());
                cacheStatsMap.put("loadFailureCount", cacheStats.loadFailureCount());
                cacheStatsMap.put("totalLoadTime", cacheStats.totalLoadTime());
                cacheStatsMap.put("evictionCount", cacheStats.evictionCount());
                cacheStatsMap.put("estimatedSize", nativeCache.estimatedSize());
                
                // Get cache configuration
                if (cache instanceof CaffeineCache) {
                    CaffeineCache caffeineCache = (CaffeineCache) cache;
                    com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeine = 
                        (com.github.benmanes.caffeine.cache.Cache<Object, Object>) caffeineCache.getNativeCache();
                    
                    Map<String, Object> config = new HashMap<>();
                    config.put("maximumSize", caffeine.policy().eviction().map(p -> p.getMaximum()).orElse(-1L));
                    cacheStatsMap.put("config", config);
                }
                
                stats.put(cacheName, cacheStatsMap);
            } else if (cache != null) {
                // Fallback for non-Caffeine caches
                Map<String, Object> cacheInfo = new HashMap<>();
                cacheInfo.put("type", cache.getNativeCache().getClass().getSimpleName());
                stats.put(cacheName, cacheInfo);
            }
        }
        
        return stats;
    }
    
    @GetMapping("/clear")
    public String clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
        return "All caches cleared successfully";
    }
}
