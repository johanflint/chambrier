package com.larastudios.chambrier.adapter

import com.larastudios.chambrier.app.ObservationException
import com.larastudios.chambrier.app.Observer
import com.larastudios.chambrier.app.domain.Device
import com.larastudios.chambrier.app.domain.DeviceType
import com.larastudios.chambrier.app.domain.DiscoveredDevices
import com.larastudios.chambrier.app.domain.Event
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2


@Service
@ConditionalOnProperty("hue.enabled")
class HueObserver(val client: HueClient) : Observer {
    override fun observe(): Flux<Event> {
        logger.info { "Retrieve Hue devices..." }
        val devices = client.retrieveDevices()
            .flatMap {
                if (it.errors.isNotEmpty()) {
                    val message = it.errors.joinToString(separator = "\n") { it.description }
                    logger.warn { "Retrieve Hue devices... failed: $message" }
                    Mono.error(ObservationException(message))
                } else {
                    logger.info { "Retrieve Hue devices... OK, ${it.data.size} found" }
                    Mono.just(it.data)
                }
            }
            .map { it.associateBy { device -> device.id } }

        logger.info { "Retrieve Hue lights..." }
        val lights = client.retrieveLights()
            .flatMap {
                if (it.errors.isNotEmpty()) {
                    val message = it.errors.joinToString(separator = "\n") { it.description }
                    logger.warn { "Retrieve Hue lights... failed: $message" }
                    Mono.error(ObservationException(message))
                } else {
                    logger.info { "Retrieve Hue lights... OK, ${it.data.size} found" }
                    Mono.just(it.data)
                }
            }

        return Mono.zip(devices, lights)
            .map { (deviceMap, lights) ->
                lights.map { light ->
                    val deviceGet = deviceMap[light.owner.rid] ?: throw ObservationException("")

                    Device(
                        deviceGet.id,
                        DeviceType.Light,
                        deviceGet.productData.manufacturerName,
                        deviceGet.productData.modelId,
                        deviceGet.productData.productName,
                        deviceGet.metadata.name,
                        mapOf()
                    )
                }
            }
            .map<Event> { DiscoveredDevices(it) }
            .flux()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
