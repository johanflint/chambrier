package com.larastudios.chambrier.adapter.shelly

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.core.publisher.Mono

class ShellyClient(webClient: WebClient) {
    private val service = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient))
        .build()
        .createClient(Api::class.java)

    suspend fun deviceInfo(): GetDeviceInfo = service.config().awaitSingle()

    suspend fun status(): GetStatus = service.status().awaitSingle()

    @HttpExchange("/rpc")
    interface Api {
        @GetExchange("/Shelly.GetDeviceInfo")
        fun config(): Mono<GetDeviceInfo>

        @GetExchange("/Shelly.GetStatus")
        fun status(): Mono<GetStatus>
    }
}
