package com.larastudios.chambrier.app

import arrow.optics.dsl.index
import arrow.optics.typeclasses.Index
import com.larastudios.chambrier.app.domain.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class PropertyChangedReducer : Reducer {
    override fun reduce(event: Event, state: State): State {
        if (event is BooleanPropertyChanged) {
            return reducePropertyChangedEvent<BooleanPropertyChanged, BooleanProperty>(event, state, { p -> "${p.value}" }) {
                it.copy(value = event.value)
            }
        }

        if (event is NumberPropertyChanged) {
            return reducePropertyChangedEvent<NumberPropertyChanged, NumberProperty>(event, state, { p -> "${p.value}" }) {
                it.copy(value = event.value)
            }
        }

        if (event is ColorPropertyChanged) {
            return reducePropertyChangedEvent<ColorPropertyChanged, ColorProperty>(event, state, { p -> "${p.xy}" }) {
                it.copy(xy = event.xy, gamut = event.gamut)
            }
        }

        if (event is EnumPropertyChanged<*>) {
            return reducePropertyChangedEvent<EnumPropertyChanged<*>, EnumProperty<*>>(event, state, { p -> "${p.value}" }) {
                if (event.value::class == it.value::class) {
                    val copy = it.copy()
                    // Due to the generics constraint it will not compile in Kotlin, fall back to Java's reflection API as there is no setter in Kotlin for vals
                    field.run {
                        isAccessible = true
                        set(copy, event.value)
                        isAccessible = false
                        copy
                    }
                } else {
                    logger.warn {
                        val eventName = event::class.simpleName
                        val device = state.devices[event.deviceId]
                        "Received $eventName event for device '${device?.id}' (${device?.name}), but enum property '${it.name}' is of type '${it.value::class.simpleName}' and event of type '${event.value::class.simpleName}': $event"
                    }
                    it
                }
            }
        }

        return state
    }

    private inline fun <reified T : PropertyChangedEvent, reified P : Property> reducePropertyChangedEvent(
        event: Event,
        state: State,
        noinline valueString: (P) -> String,
        noinline map: (P) -> P,
    ): State {
        if (event is T) {
            val eventName = T::class.simpleName
            val device = state.devices[event.deviceId]
            if (device == null) {
                logger.warn { "Received $eventName event for unknown device '${event.deviceId}': $event" }
                return state
            }

            val property = device.properties[event.propertyId]
            if (property == null || property !is P) {
                logger.warn { "Received $eventName event for device '${device.id}' (${device.name}), but for unknown property '${event.propertyId}': $event" }
                return state
            }

            val updatedProperty = map(property)
            return State.devices.index(Index.map(), device.id)
                .properties.index(Index.map(), property.name)
                .modify(state) { map(property) }
                .also {
                    logger.debug { "Set property '${property.name}' for device '${device.id}' (${device.name}) to '${valueString(updatedProperty)}', was '${valueString(property)}'" }
                }
        }

        return state
    }

    companion object {
        private val field = EnumProperty::class.java.getDeclaredField(EnumProperty<*>::value.name)
        private val logger = KotlinLogging.logger {}
    }
}
