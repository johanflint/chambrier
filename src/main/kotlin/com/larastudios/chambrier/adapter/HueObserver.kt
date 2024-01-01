package com.larastudios.chambrier.adapter

import com.larastudios.chambrier.app.ObservationException
import com.larastudios.chambrier.app.Observer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class HueObserver(val client: HueClient) : Observer {
    override fun observe() {
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

        Mono.zip(devices, lights).subscribe()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
