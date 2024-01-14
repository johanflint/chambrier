package com.larastudios.chambrier.adapter

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.core.publisher.Mono

@Service
class HueClient(webClient: WebClient) {
    private val service = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient))
        .build()
        .createClient(Api::class.java)

    fun retrieveDevices(): Mono<DevicesResponse> = service.devices()

    fun retrieveLights(): Mono<LightsResponse> = service.lights()

    fun retrieveButtons(): Mono<ButtonResponse> = service.buttons()

    @HttpExchange("/clip/v2/resource")
    interface Api {
        @GetExchange("/device")
        fun devices(): Mono<DevicesResponse>

        @GetExchange("/light")
        fun lights(): Mono<LightsResponse>

        @GetExchange("/button")
        fun buttons(): Mono<ButtonResponse>
    }
}
