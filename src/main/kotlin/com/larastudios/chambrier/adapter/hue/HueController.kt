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

            val brightnessProperty = device.properties.values.firstOrNull { it.type == PropertyType.Brightness } as? NumberProperty
            val brightness = brightnessProperty?.let {
                val currentValue = brightnessProperty.value?.toDouble() ?: 0.0
                when (val propertyValue = command.propertyMap[it.name]) {
                    is SetNumberValue -> propertyValue.value
                    is IncrementNumberValue -> currentValue + propertyValue.value.toDouble()
                    is DecrementNumberValue -> currentValue - propertyValue.value.toDouble()
                    else -> null
                }?.let {
                    val minimumValue = brightnessProperty.minimum?.toDouble() ?: Double.MIN_VALUE
                    val maximumValue = brightnessProperty.maximum?.toDouble() ?: Double.MAX_VALUE
                    val value = it.toDouble().coerceIn(minimumValue, maximumValue)

                    logger.info { "Change property '${brightnessProperty.name}' of device '${device.name}' to '$value' from '$currentValue'..." }
                    SetDimming(brightness = value)
                }
            }

            if (onProperty != null && (on != null || brightness != null)) {
                client.controlLight(onProperty.externalId!!, LightRequest(on = on, dimming = brightness))
                    .log()
                    .subscribe()
            }
        }
    }


    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
