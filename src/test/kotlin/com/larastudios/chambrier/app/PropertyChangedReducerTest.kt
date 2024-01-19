package com.larastudios.chambrier.app

import com.larastudios.chambrier.app.domain.*
import com.larastudios.chambrier.app.domain.Unit
import com.larastudios.chambrier.lightDevice
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("PropertyChangedReducer")
class PropertyChangedReducerTest {
    private val booleanProperty = BooleanProperty("on", PropertyType.On, readonly = false, value = true)
    private val numberProperty = NumberProperty("brightness", PropertyType.Brightness, readonly = false, Unit.Percentage, 42, null, null)

    private val device: Device = lightDevice.copy(properties = mapOf(booleanProperty.name to booleanProperty, numberProperty.name to numberProperty))
    private val initialState: State = State(mapOf(device.id to device))

    @Test
    fun `reduces BooleanPropertyChanged by updating the device's property`() {
        val event = BooleanPropertyChanged(device.id, booleanProperty.name, false)
        val newState = PropertyChangedReducer().reduce(event, initialState)

        assertThat(newState.devices)
            .extractingByKey(device.id)
            .extracting { it.properties[booleanProperty.name] }
            .isEqualTo(BooleanProperty("on", PropertyType.On, readonly = false, value = false))
    }

    @Test
    fun `reduces NumberPropertyChanged by updating the device's property`() {
        val event = NumberPropertyChanged(device.id, numberProperty.name, 1337)
        val newState = PropertyChangedReducer().reduce(event, initialState)

        assertThat(newState.devices)
            .extractingByKey(device.id)
            .extracting { it.properties[numberProperty.name] }
            .isEqualTo(numberProperty.copy(value = 1337))
    }

    @Test
    fun `ignores the event by returning the unmodified state for an unknown device`() {
        val event = NumberPropertyChanged("unknown", numberProperty.name, 1337)
        val newState = PropertyChangedReducer().reduce(event, initialState)

        assertThat(newState).isEqualTo(initialState)
    }

    @Test
    fun `ignores the event by returning the unmodified state for a known device but an unknown property`() {
        val event = NumberPropertyChanged(device.id, "unknown", 1337)
        val newState = PropertyChangedReducer().reduce(event, initialState)

        assertThat(newState).isEqualTo(initialState)
    }

    @Test
    fun `ignores other events by returning the unmodified state`() {
        val event = mockk<Event>()
        val newState = PropertyChangedReducer().reduce(event, initialState)

        assertThat(newState).isEqualTo(initialState)
    }
}
