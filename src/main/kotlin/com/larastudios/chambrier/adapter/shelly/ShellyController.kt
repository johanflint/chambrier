package com.larastudios.chambrier.adapter.shelly

import com.fasterxml.jackson.databind.ObjectMapper
import com.larastudios.chambrier.app.Controller
import com.larastudios.chambrier.app.domain.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class ShellyController(
    val objectMapper: ObjectMapper,
    val webClientBuilder: WebClient.Builder
) : Controller {
    override suspend fun send(commands: List<DeviceCommand>) {
        commands.filterIsInstance<ControlDeviceCommand>()
            .filter { it.device.manufacturer == "Shelly" }
            .forEach { send(it) }
    }

    private suspend fun send(command: ControlDeviceCommand) {
        logger.info { "Sending command $command" }
        val device = command.device

        val webClient = webClientBuilder.baseUrl(device.address!!)
            .codecs { it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper)) }
            .build()
        val client = ShellyClient(webClient)

        val onProperty = device.properties.values.firstOrNull { it.type == PropertyType.On } as? BooleanProperty
        if (onProperty == null) {
            return
        }

        when (val propertyValue = command.propertyMap[onProperty.name]) {
            is SetBooleanValue -> propertyValue.value
            is ToggleBooleanValue -> !onProperty.value
            else -> null
        }?.let { value ->
            logger.debug { "Change property '${onProperty.name}' of device '${device.name}' to '$value' from '${onProperty.value}'..." }
            client.switchSet(SwitchSetRequestBody(device.externalId?.toInt() ?: 0, value))
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
