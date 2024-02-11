package com.larastudios.chambrier.adapter.hue

import com.larastudios.chambrier.app.Controller
import com.larastudios.chambrier.app.domain.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class HueController(val client: HueClient) : Controller {
    override suspend fun send(commands: List<DeviceCommand>) {
        commands.filterIsInstance<ControlDeviceCommand>().forEach { send(it) }
    }

    private suspend fun send(command: ControlDeviceCommand) {
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
                val currentValue = brightnessProperty.value ?: 0
                when (val propertyValue = command.propertyMap[it.name]) {
                    is SetNumberValue -> propertyValue.value
                    is IncrementNumberValue -> currentValue + propertyValue.value
                    is DecrementNumberValue -> currentValue - propertyValue.value
                    else -> null
                }?.let {
                    val value = it.coerceInNullable(brightnessProperty.minimum, brightnessProperty.maximum)

                    logger.info { "Change property '${brightnessProperty.name}' of device '${device.name}' to '$value' from '$currentValue'..." }
                    SetDimming(brightness = value)
                }
            }

            val colorProperty = device.properties.values.firstOrNull { it.type == PropertyType.Color } as? ColorProperty
            val color = colorProperty?.let {
                val propertyValue = command.propertyMap[it.name] as? SetColorValue
                propertyValue?.let {
                    logger.info { "Change property '${colorProperty.name}' of device '${device.name}' to '${it.xy}' from '${colorProperty.xy}'..." }
                    SetColor(xy = it.xy)
                }
            }

            val colorTemperatureProperty = device.properties.values.firstOrNull { it.type == PropertyType.ColorTemperature } as? NumberProperty
            val colorTemperature = colorTemperatureProperty?.let {
                val propertyValue = command.propertyMap[it.name] as? SetNumberValue
                propertyValue?.let {
                    val kelvin = it.value.coerceInNullable(colorTemperatureProperty.minimum, colorTemperatureProperty.maximum)
                    logger.info { "Change property '${colorTemperatureProperty.name}' of device '${device.name}' to '$kelvin' from '${colorTemperatureProperty.value}'..." }
                    SetColorTemperature(kelvinToMirek(kelvin))
                }
            }

            if (onProperty != null && (on != null || brightness != null || colorTemperature != null || color != null)) {
                val response = client.controlLight(onProperty.externalId!!, LightRequest(on = on, dimming = brightness, colorTemperature = colorTemperature, color = color))
                logger.debug { "Control light response: $response" }
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
