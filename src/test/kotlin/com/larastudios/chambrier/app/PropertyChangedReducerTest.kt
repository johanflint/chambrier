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
    private val gamut = Gamut(
        red = CartesianCoordinate(0.1, 0.2),
        green = CartesianCoordinate(0.3, 0.4),
        blue = CartesianCoordinate(0.5, 0.6),
    )
    private val colorProperty = ColorProperty("color", PropertyType.Color, readonly = false, xy = CartesianCoordinate(0.01, 0.05), gamut = gamut)
    private val enumProperty = EnumProperty("button", PropertyType.Button, readonly = true, values = HueButtonState.entries.toList(), value =  HueButtonState.InitialPress)

    private val device: Device = lightDevice.copy(
        properties = mapOf(
            booleanProperty.name to booleanProperty,
            numberProperty.name to numberProperty,
            colorProperty.name to colorProperty,
            enumProperty.name to enumProperty,
        )
    )
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
    fun `reduces ColorPropertyChanged without gamut by updating the device's property`() {
        val event = ColorPropertyChanged(device.id, colorProperty.name, CartesianCoordinate(0.90, 0.91), null)
        val newState = PropertyChangedReducer().reduce(event, initialState)

        assertThat(newState.devices)
            .extractingByKey(device.id)
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
        val event = ColorPropertyChanged(device.id, colorProperty.name, CartesianCoordinate(0.90, 0.91), gamut)
        val newState = PropertyChangedReducer().reduce(event, initialState)

        assertThat(newState.devices)
            .extractingByKey(device.id)
            .extracting { it.properties[colorProperty.name] }
            .isEqualTo(colorProperty.copy(xy = CartesianCoordinate(0.90, 0.91), gamut = gamut))
    }

    @Test
    fun `reduces EnumPropertyChanged by updating the device's property`() {
        val event = EnumPropertyChanged(device.id, enumProperty.name, HueButtonState.ShortRelease)
        val newState = PropertyChangedReducer().reduce(event, initialState)

        assertThat(newState.devices)
            .extractingByKey(device.id)
            .extracting { it.properties[enumProperty.name] }
            .isEqualTo(enumProperty.copy(value = HueButtonState.ShortRelease))
    }

    @Test
    fun `ignores EnumPropertyChanged by returning the unmodified state if the enum types do not match`() {
        val event = EnumPropertyChanged(device.id, enumProperty.name, DifferentEnum.NotCool)
        val newState = PropertyChangedReducer().reduce(event, initialState)

        assertThat(newState.devices)
            .extractingByKey(device.id)
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

private enum class DifferentEnum { NotCool }
