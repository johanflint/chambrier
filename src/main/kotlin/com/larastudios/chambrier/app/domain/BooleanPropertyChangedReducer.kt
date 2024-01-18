package com.larastudios.chambrier.app.domain

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class BooleanPropertyChangedReducer : Reducer {
    override fun reduce(event: Event, state: State): State {
        if (event is BooleanPropertyChanged) {
            val device = state.devices[event.deviceId]
            if (device == null) {
                logger.warn { "Received BooleanPropertyChanged event for unknown device '${event.deviceId}': $event" }
                return state
            }

            val property = device.properties[event.propertyId]
            if (property == null || property !is BooleanProperty) {
                logger.warn { "Received BooleanPropertyChanged event for device '${device.id}', but for unknown property '${event.propertyId}': $event" }
                return state
            }

            logger.debug { "Set property '${property.name}' for device '${device.id}' to '${event.value}', was '${property.value}'" }

            val updatedProperty = property.copy(value = event.value)
            val updatedProperties = device.properties.toMutableMap().apply {
                replace(updatedProperty.name, updatedProperty)
            }.toMap()

            val updatedDevice = device.copy(properties = updatedProperties)
            val updatedDevices = state.devices.toMutableMap().apply {
                replace(device.id, updatedDevice)
            }.toMap()
            return state.copy(devices = updatedDevices)
        }

        return state
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
