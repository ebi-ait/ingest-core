package org.humancellatlas.ingest.security.authn.provider.elixir;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Component that monitors and logs cache statistics.
 * Prints cache stats every 30 seconds to help with monitoring and debugging.
 * 
 * Note: This version works with any CacheManager implementation.
 * For detailed Caffeine statistics, ensure CaffeineCacheManager is properly configured.
 */
@Component
public class CacheReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheReporter.class);

    @Autowired
    private CacheManager cacheManager;

    /**
     * Prints cache statistics every 30 seconds.
     * This helps monitor cache performance and usage patterns.
     */
    @Scheduled(fixedRate = 60000) 
    public void printCacheStats() {
        try {
            LOGGER.info("=== Cache Statistics ===");
            
            // Print basic cache information
            LOGGER.debug("Cache Manager Type: {}", cacheManager.getClass().getSimpleName());
            LOGGER.debug("Number of Caches: {}", cacheManager.getCacheNames().size());
            
            // Print info for each cache
            for (String cacheName : cacheManager.getCacheNames()) {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    LOGGER.debug("Cache: {} - Type: {}", cacheName, cache.getClass().getSimpleName());
                    
                    // Try to get more detailed info if it's a Caffeine cache
                    try {
                        // Use reflection to check if it's a Caffeine cache with stats
                        Object nativeCache = cache.getNativeCache();
                        if (nativeCache != null) {
                            LOGGER.debug("  Native Cache Type: {}", nativeCache.getClass().getSimpleName());
                            
                            // Try to get size if available
                            if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache) {
                                com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache = 
                                    (com.github.benmanes.caffeine.cache.Cache<?, ?>) nativeCache;
                                LOGGER.debug("  Estimated Size: {}", caffeineCache.estimatedSize());
                                
                                // Try to get stats if available
                                try {
                                    com.github.benmanes.caffeine.cache.stats.CacheStats stats = caffeineCache.stats();
                                    LOGGER.debug("  Hit Count: {}", stats.hitCount());
                                    LOGGER.debug("  Miss Count: {}", stats.missCount());
                                    LOGGER.debug("  Hit Rate: {:.2f}%", stats.hitRate() * 100);
                                    LOGGER.debug("  Request Count: {}", stats.requestCount());
                                    LOGGER.debug("  Eviction Count: {}", stats.evictionCount());
                                } catch (Exception e) {
                                    LOGGER.debug("Could not get detailed stats: {}", e.getMessage());
                                }
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.debug("Could not get native cache info for {}: {}", cacheName, e.getMessage());
                    }
                } else {
                    LOGGER.warn("Cache '{}' is null", cacheName);
                }
            }
            
            LOGGER.debug("========================");
            
        } catch (Exception e) {
            LOGGER.error("Error printing cache statistics", e);
        }
    }



}
