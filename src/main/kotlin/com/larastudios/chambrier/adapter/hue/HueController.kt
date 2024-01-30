package com.larastudios.chambrier.adapter.hue

import com.larastudios.chambrier.app.Controller
import com.larastudios.chambrier.app.domain.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class HueController(val client: HueClient) : Controller {
    override fun send(commands: List<DeviceCommand>) {
        commands.filterIsInstance<ControlDeviceCommand>().forEach(::send)
    }

    private fun send(command: ControlDeviceCommand) {
        logger.info { "Sending command $command" }
        val device = command.device

        if (device.type == DeviceType.Light) {
            val onProperty = device.properties.values.firstOrNull { it.type == PropertyType.On } as? BooleanProperty
            val on = onProperty?.let {
                when (val propertyValue = command.propertyMap[it.name]) {
                    is SetBooleanValue -> On(propertyValue.value)
                    is ToggleBooleanValue -> On(!it.value)
                    else -> null
                }?.apply { logger.info { "Change property '${onProperty.name}' of device '${device.name}' to '$on' from '${onProperty.value}'..." } }
            }

            if (on != null) {
                client.controlLight(onProperty.externalId!!, LightRequest(on = on))
                    .log()
                    .subscribe()
            }
        }
    }


    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
