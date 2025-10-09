package org.humancellatlas.ingest.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Arrays;


@Configuration
@EnableCaching
public class CaffeineCacheConfig {

    /**
     * Cache names used throughout the application
     */
    public static final String USER_INFO_CACHE = "userInfo";
    public static final String JWT_VERIFICATION_CACHE = "jwtVerification";

    /**
     * Configures the Caffeine cache manager with appropriate settings.
     * 
     * @return CacheManager instance configured with Caffeine
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Configure Caffeine cache with appropriate settings
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)                    // Maximum number of entries
                .expireAfterWrite(Duration.ofMinutes(1))  // Expire after 10 minutes of write
                .expireAfterAccess(Duration.ofMinutes(1))  // Expire after 5 minutes of access
                .recordStats()                        // Enable cache statistics
        );
        
        // Set cache names
        cacheManager.setCacheNames(Arrays.asList(
            USER_INFO_CACHE,
            JWT_VERIFICATION_CACHE
        ));
        
        return cacheManager;
    }



}
