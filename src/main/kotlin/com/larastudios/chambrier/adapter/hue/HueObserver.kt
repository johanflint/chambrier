package com.larastudios.chambrier.adapter.hue

import com.larastudios.chambrier.adapter.hue.sse.*
import com.larastudios.chambrier.app.ObservationException
import com.larastudios.chambrier.app.Observer
import com.larastudios.chambrier.app.domain.DiscoveredDevices
import com.larastudios.chambrier.app.domain.Event
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
@ConditionalOnProperty("hue.enabled")
class HueObserver(val client: HueClient) : Observer {
    override suspend fun observe(): Flow<Event> = coroutineScope {
        logger.info { "Retrieve Hue devices..." }
        val deferredDevices = client.retrieveDevices()

        logger.info { "Retrieve Hue lights..." }
        val deferredLights = client.retrieveLights()

        logger.info { "Retrieve Hue switches..." }
        val deferredButtons = client.retrieveButtons()

        awaitAll(deferredDevices, deferredLights, deferredButtons)

        val devices = deferredDevices.await()
            .extractData("devices")
            .associateBy { device -> device.id }
        val lights = deferredLights.await().extractData("lights")
        val buttons = deferredButtons.await().extractData("switches")

        val lightDevices = mapLights(lights, devices)
        val switchDevices = mapSwitches(buttons, devices)

        val sseStream = client.sse()
        val eventStream = sseStream
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
            .flatMapIterable { it }

        val event: Event = DiscoveredDevices(lightDevices + switchDevices)
        Mono.just(event)
            .concatWith(eventStream)
            .asFlow()
    }

    private fun <T> HueResponse<T>.extractData(type: String): List<T> =
        if (errors.isNotEmpty()) {
            val message = errors.joinToString(separator = "\n") { it.description }
            logger.warn { "Retrieve Hue $type... failed: $message" }
            throw ObservationException(message)
        } else {
            logger.info { "Retrieve Hue $type... OK, ${data.size} found" }
            data
        }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
