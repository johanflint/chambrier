package com.larastudios.chambrier

import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RateLimiterConfiguration {
    @Bean
    fun hueRatelimiter(registry: RateLimiterRegistry): RateLimiter = registry.rateLimiter("hue")
}
