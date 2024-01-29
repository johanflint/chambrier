package com.larastudios.chambrier.app

import com.larastudios.chambrier.*
import com.larastudios.chambrier.app.domain.*
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("PropertyChangedReducer")
class PropertyChangedReducerTest {
    @Test
    fun `reduces BooleanPropertyChanged by updating the device's property`() {
        val event = BooleanPropertyChanged(lightDevice.id, booleanProperty.name, false)
        val newState = PropertyChangedReducer().reduce(event, initialState)

        assertThat(newState.devices)
            .extractingByKey(lightDevice.id)
            .extracting { it.properties[booleanProperty.name] }
            .isEqualTo(BooleanProperty("on", PropertyType.On, readonly = false, value = false))
    }

    @Test
    fun `reduces NumberPropertyChanged by updating the device's property`() {
        val event = NumberPropertyChanged(lightDevice.id, numberProperty.name, 1337)
        val newState = PropertyChangedReducer().reduce(event, initialState)

        assertThat(newState.devices)
            .extractingByKey(lightDevice.id)
            .extracting { it.properties[numberProperty.name] }
            .isEqualTo(numberProperty.copy(value = 1337))
    }

    @Test
    fun `reduces ColorPropertyChanged without gamut by updating the device's property`() {
        val event = ColorPropertyChanged(lightDevice.id, colorProperty.name, CartesianCoordinate(0.90, 0.91), null)
        val newState = PropertyChangedReducer().reduce(event, initialState)

        assertThat(newState.devices)
            .extractingByKey(lightDevice.id)
            .extracting { it.properties[colorProperty.name] }
            .isEqualTo(colorProperty.copy(xy = CartesianCoordinate(0.90, 0.91), gamut = null))
    }

    @Test
    fun `reduces ColorPropertyChanged with gamut by updating the device's property`() {
        val gamut = Gamut(
            red = CartesianCoordinate(0.80, 0.81),
            green = CartesianCoordinate(0.82, 0.83),
            blue = CartesianCoordinate(0.84, 0.85),
        )
        val event = ColorPropertyChanged(lightDevice.id, colorProperty.name, CartesianCoordinate(0.90, 0.91), gamut)
        val newState = PropertyChangedReducer().reduce(event, initialState)

        assertThat(newState.devices)
            .extractingByKey(lightDevice.id)
            .extracting { it.properties[colorProperty.name] }
            .isEqualTo(colorProperty.copy(xy = CartesianCoordinate(0.90, 0.91), gamut = gamut))
    }

    @Test
    fun `reduces EnumPropertyChanged by updating the device's property`() {
        val event = EnumPropertyChanged(editableSwitchDevice.id, enumProperty.name, HueButtonState.ShortRelease)
        val newState = PropertyChangedReducer().reduce(event, initialState)

        assertThat(newState.devices)
            .extractingByKey(editableSwitchDevice.id)
            .extracting { it.properties[enumProperty.name] }
            .isEqualTo(enumProperty.copy(value = HueButtonState.ShortRelease))
    }

    @Test
    fun `ignores EnumPropertyChanged by returning the unmodified state if the enum types do not match`() {
        val event = EnumPropertyChanged(editableSwitchDevice.id, enumProperty.name, DifferentEnum.NotCool)
        val newState = PropertyChangedReducer().reduce(event, initialState)

        assertThat(newState.devices)
            .extractingByKey(editableSwitchDevice.id)
            .extracting { it.properties[enumProperty.name] }
            .isEqualTo(enumProperty)
    }

    @Test
    fun `ignores the event by returning the unmodified state for an unknown device`() {
        val event = NumberPropertyChanged("unknown", numberProperty.name, 1337)
        val newState = PropertyChangedReducer().reduce(event, initialState)

        assertThat(newState).isEqualTo(initialState)
    }

    @Test
    fun `ignores the event by returning the unmodified state for a known device but an unknown property`() {
        val event = NumberPropertyChanged(lightDevice.id, "unknown", 1337)
        val newState = PropertyChangedReducer().reduce(event, initialState)

        assertThat(newState).isEqualTo(initialState)
    }

    @Test
    fun `ignores the event by returning the unmodified state for read-only property`() {
        val device: Device = lightDevice.copy(
            properties = mapOf(numberProperty.name to numberProperty.copy(readonly = true))
        )
        val initialState = State(mapOf(device.id to device))

        val event = NumberPropertyChanged(device.id, numberProperty.name, 1337)
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

private enum class DifferentEnum { NotCool }
