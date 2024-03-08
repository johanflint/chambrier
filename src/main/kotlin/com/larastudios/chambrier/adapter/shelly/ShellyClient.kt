package com.larastudios.chambrier.adapter.shelly

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.core.publisher.Mono

class ShellyClient(webClient: WebClient) {
    private val service = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient))
        .build()
        .createClient(Api::class.java)

    suspend fun deviceInfo(): GetDeviceInfo = service.config().awaitSingle()

    suspend fun status(): GetStatus = service.status().awaitSingle()

    suspend fun listHooks(): List<Webhook> = service.listHooks().awaitSingle().hooks

    suspend fun createHook(body: WebhookCreateRequestBody): WebhookCreatedResponse = service.createHook(body).awaitSingle()

    @HttpExchange("/rpc")
    interface Api {
        @GetExchange("/Shelly.GetDeviceInfo")
        fun config(): Mono<GetDeviceInfo>

        @GetExchange("/Shelly.GetStatus")
        fun status(): Mono<GetStatus>

        @GetExchange("/Webhook.List")
        fun listHooks(): Mono<WebhookList>

        @PostExchange("/Webhook.Create")
        fun createHook(@RequestBody body: WebhookCreateRequestBody): Mono<WebhookCreatedResponse>
    }
}
