package com.larastudios.chambrier

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Configuration
class WebClientConfiguration {
    @Bean
    fun hueWebClient(
        @SnakeCased objectMapper: ObjectMapper,
        webClientBuilder: WebClient.Builder,
        @Value("\${hue.base-url}") baseUrl: String,
        @Value("\${hue.app-key}") appKey: String,
    ): WebClient {
        // Ignoring the certificate as Hue uses a self-signed root certificate which is not trusted
        val sslContext = SslContextBuilder.forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .build()

        return webClientBuilder
            .clientConnector(ReactorClientHttpConnector(HttpClient.create().secure {
                it.sslContext(sslContext)
            }))
            .baseUrl(baseUrl)
            .defaultHeader("hue-application-key", appKey)
            .codecs {
                it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper))
            }
            .build()
    }
}
