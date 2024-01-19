package com.larastudios.chambrier.app

import com.larastudios.chambrier.app.domain.*
import com.larastudios.chambrier.app.domain.Unit
import com.larastudios.chambrier.lightDevice
import io.mockk.mockk
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("NumberPropertyChangedReducer")
class NumberPropertyChangedReducerTest {
    private val numberProperty: NumberProperty = NumberProperty("brightness", PropertyType.Brightness, readonly = false, Unit.Percentage, 42, null, null)
    private val device: Device = lightDevice.copy(properties = mapOf(numberProperty.name to numberProperty))
    private val initialState: State = State(mapOf(device.id to device))

    @Test
    fun `reduces NumberPropertyChanged by updating the device's property`() {
        val event = NumberPropertyChanged(device.id, numberProperty.name, 1337)
        val newState = NumberPropertyChangedReducer().reduce(event, initialState)

        assertThat(newState.devices)
            .extractingByKey(device.id)
            .extracting { it.properties[numberProperty.name] }
            .isEqualTo(numberProperty.copy(value = 1337))
    }

    @Test
    fun `ignores the event by returning the unmodified state for an unknown device`() {
        val event = NumberPropertyChanged("unknown", numberProperty.name, 1337)
        val newState = NumberPropertyChangedReducer().reduce(event, initialState)

        assertThat(newState).isEqualTo(initialState)
    }

    @Test
    fun `ignores the event by returning the unmodified state for a known device but an unknown property`() {
        val event = NumberPropertyChanged(device.id, "unknown", 1337)
        val newState = NumberPropertyChangedReducer().reduce(event, initialState)

        assertThat(newState).isEqualTo(initialState)
    }

    @Test
    fun `ignores other events by returning the unmodified state`() {
        val event = mockk<Event>()
        val newState = NumberPropertyChangedReducer().reduce(event, initialState)

        assertThat(newState).isEqualTo(initialState)
    }
}
