package com.larastudios.chambrier.adapter.hue

import com.larastudios.chambrier.adapter.hue.sse.*
import com.larastudios.chambrier.app.ObservationException
import com.larastudios.chambrier.app.Observer
import com.larastudios.chambrier.app.domain.DiscoveredDevices
import com.larastudios.chambrier.app.domain.Event
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.kotlin.core.util.function.component3

@Service
@ConditionalOnProperty("hue.enabled")
class HueObserver(val client: HueClient) : Observer {
    override fun observe(): Flux<Event> {
        logger.info { "Retrieve Hue devices..." }
        val devices = client.retrieveDevices()
            .flatMap { it.extractData("devices") }
            .map { it.associateBy { device -> device.id } }

        logger.info { "Retrieve Hue lights..." }
        val lights = client.retrieveLights()
            .flatMap { it.extractData("lights") }

        logger.info { "Retrieve Hue switches..." }
        val buttons = client.retrieveButtons()
            .flatMap { it.extractData("switches") }

        val sseStream = client.sse()
        sseStream
            .take(1)
            .handle { event, sink ->
                if (event.comment() != "hi" || event.data() != null) {
                    sink.error(IllegalArgumentException("Unexpected first event"))
                } else {
                    sink.next(event)
                }
            }
            .concatWith(sseStream.skip(1))
            .flatMapIterable { it.data() ?: listOf() } // Events may not have data
            .filter { it.type == SseDataType.Update }
            .flatMapIterable { it.data }
            .map {
                when (it) {
                    is ChangedLightProperty -> mapChangedLightProperty(it).toList()
                    is ChangedButtonProperty -> mapChangedButtonProperty(it).toList()
                    else -> listOf()
                }
            }
            .log()
            .subscribe()

        return Mono.zip(devices, lights, buttons)
            .map { (deviceMap, lights, buttons) ->
                val lightDevices = mapLights(lights, deviceMap)
                val switchDevices = mapSwitches(buttons, deviceMap)

                lightDevices + switchDevices
            }
            .map<Event> { DiscoveredDevices(it) }
            .flux()
    }

    private fun <T> HueResponse<T>.extractData(type: String): Mono<List<T>> =
        if (errors.isNotEmpty()) {
            val message = errors.joinToString(separator = "\n") { it.description }
            logger.warn { "Retrieve Hue $type... failed: $message" }
            Mono.error(ObservationException(message))
        } else {
            logger.info { "Retrieve Hue $type... OK, ${data.size} found" }
            Mono.just(data)
        }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
