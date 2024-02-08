package com.larastudios.chambrier.adapter.hue

import com.larastudios.chambrier.adapter.hue.sse.SseData
import io.github.resilience4j.kotlin.ratelimiter.executeSuspendFunction
import io.github.resilience4j.ratelimiter.RateLimiter
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PutExchange
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration

/**
 * Client to communicate with a Hue bridge.
 *
 * To ensure not to flood the bridge with requests, a rate limiter is used.
 * https://developers.meethue.com/develop/application-design-guidance/hue-system-performance/
 */
@Service
class HueClient(private val webClient: WebClient, private val rateLimiter: RateLimiter) {
    private val service = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient))
        .build()
        .createClient(Api::class.java)

    fun retrieveDevices(): Mono<HueResponse<DeviceGet>> = service.devices()

    fun retrieveLights(): Mono<HueResponse<LightGet>> = service.lights()

    fun retrieveButtons(): Mono<HueResponse<ButtonGet>> = service.buttons()

    suspend fun controlLight(lightId: String, @RequestBody requestBody: LightRequest): String = rateLimiter.executeSuspendFunction {
        service.light(lightId, requestBody).awaitSingle()
    }

    fun sse(): Flux<ServerSentEvent<List<SseData>>> {
        val type = object : ParameterizedTypeReference<ServerSentEvent<List<SseData>>>() {}
        return webClient
            .get()
            .uri("/eventstream/clip/v2")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(type)
            .timeout(Duration.ofMinutes(5))
            .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofMillis(500)).maxBackoff(Duration.ofMinutes(1)))
    }

    @HttpExchange("/clip/v2/resource")
    interface Api {
        @GetExchange("/device")
        fun devices(): Mono<HueResponse<DeviceGet>>

        @GetExchange("/light")
        fun lights(): Mono<HueResponse<LightGet>>

        @PutExchange("/light/{lightId}")
        fun light(@PathVariable lightId: String, @RequestBody requestBody: LightRequest): Mono<String>

        @GetExchange("/button")
        fun buttons(): Mono<HueResponse<ButtonGet>>
    }
}
