package com.larastudios.chambrier.adapter

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.core.publisher.Mono

@Service
class HueClient(val webClient: WebClient) {
    fun retrieveDevices(): Mono<DevicesResponse> {
        val factory = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build()
        val service = factory.createClient(Api::class.java)

        return service.devices()
    }

    interface Api {
        @GetExchange("/clip/v2/resource/device")
        fun devices(): Mono<DevicesResponse>
    }
}
