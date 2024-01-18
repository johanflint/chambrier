package com.larastudios.chambrier.app.domain

import com.larastudios.chambrier.lightDevice
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("BooleanPropertyChangedReducer")
class BooleanPropertyChangedReducerTest {
    private val booleanProperty: Property = BooleanProperty("on", PropertyType.On, readonly = false, value = true)
    private val device: Device = lightDevice.copy(properties = mapOf(booleanProperty.name to booleanProperty))
    private val initialState: State = State(mapOf(device.id to device))

    @Test
    fun `reduces BooleanPropertyChanged by updating the device's property`() {
        val event = BooleanPropertyChanged(device.id, booleanProperty.name, false)
        val newState = BooleanPropertyChangedReducer().reduce(event, initialState)

        assertThat(newState.devices)
            .extractingByKey(device.id)
            .extracting { it.properties[booleanProperty.name] }
            .isEqualTo(BooleanProperty("on", PropertyType.On, readonly = false, value = false))
    }

    @Test
    fun `ignores the event by returning the unmodified state for an unknown device`() {
        val event = BooleanPropertyChanged("unknown", booleanProperty.name, false)
        val newState = BooleanPropertyChangedReducer().reduce(event, initialState)

        assertThat(newState).isEqualTo(initialState)
    }

    @Test
    fun `ignores the event by returning the unmodified state for a known device but an unknown property`() {
        val event = BooleanPropertyChanged(device.id, "unknown", false)
        val newState = BooleanPropertyChangedReducer().reduce(event, initialState)

        assertThat(newState).isEqualTo(initialState)
    }

    @Test
    fun `ignores other events by returning the unmodified state`() {
        val event = mockk<Event>()
        val newState = BooleanPropertyChangedReducer().reduce(event, initialState)

        assertThat(newState).isEqualTo(initialState)
    }
}
