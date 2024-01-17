package com.larastudios.chambrier.adapter.hue

import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration

@Service
class HueClient(private val webClient: WebClient) {
    private val service = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient))
        .build()
        .createClient(Api::class.java)

    fun retrieveDevices(): Mono<HueResponse<DeviceGet>> = service.devices()

    fun retrieveLights(): Mono<HueResponse<LightGet>> = service.lights()

    fun retrieveButtons(): Mono<HueResponse<ButtonGet>> = service.buttons()

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

        @GetExchange("/button")
        fun buttons(): Mono<HueResponse<ButtonGet>>
    }
}
