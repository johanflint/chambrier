package com.larastudios.chambrier.app

import arrow.optics.dsl.index
import arrow.optics.typeclasses.Index
import com.larastudios.chambrier.app.domain.*
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
                logger.warn { "Received BooleanPropertyChanged event for device '${device.id}' (${device.name}), but for unknown property '${event.propertyId}': $event" }
                return state
            }

            return State.devices.index(Index.map(), device.id)
                .properties.index(Index.map(), property.name)
                .set(state, property.copy(value = event.value))
                .also { logger.debug { "Set property '${property.name}' for device '${device.id}' (${device.name}) to '${event.value}', was '${property.value}'" } }
        }

        return state
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
