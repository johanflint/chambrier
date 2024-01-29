package com.larastudios.chambrier.app.flowEngine

import com.larastudios.chambrier.app.domain.*
import com.larastudios.chambrier.app.domain.Unit
import com.larastudios.chambrier.lightDevice
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("ControlDeviceAction")
class ControlDeviceActionTest {
    private val property = mapOf("on" to SetBooleanValue(true))
    private val propertyTwo = mapOf("brightness" to IncrementNumberValue(1))
    private val propertyThree = mapOf("on" to SetBooleanValue(false))
    private val action = ControlDeviceAction("42", property)
    private val context = FlowContext(state = State(devices = mapOf("42" to lightDevice.copy(properties = mapOf(
        "on" to BooleanProperty("on", PropertyType.Button, readonly = false, true),
        "brightness" to NumberProperty("brightness", PropertyType.Brightness, readonly = false, Unit.Percentage, 42, null, null)
    )))))

    @Test
    fun `does not modify the scope if the device is unknown `() {
        val scope = Scope()

        action.execute(FlowContext(State(mapOf())), scope)

        assertThat(scope.data).isEmpty()
    }

    @Test
    fun `ignores all properties if the device is known but all properties are unknown`() {
        val scope = Scope()

        val action = ControlDeviceAction("42", mapOf("unknownProperty" to SetBooleanValue(true)))
        action.execute(context, scope)

        assertThat(scope.data).isEmpty()
    }

    @Test
    fun `adds the known properties while ignoring unknown properties`() {
        val scope = Scope()

        val action = ControlDeviceAction("42", mapOf("unknownProperty" to SetBooleanValue(true)) + property)
        action.execute(context, scope)

        assertThat(scope.data).containsKey(ControlDeviceAction.COMMAND_MAP)
        assertThat(scope.data[ControlDeviceAction.COMMAND_MAP]).isEqualTo(mutableMapOf("42" to property))
    }

    @Test
    fun `adds the deviceId and properties if the command map is absent from the scope`() {
        val scope = Scope()

        action.execute(context, scope)

        assertThat(scope.data).containsKey(ControlDeviceAction.COMMAND_MAP)
        assertThat(scope.data[ControlDeviceAction.COMMAND_MAP]).isEqualTo(mutableMapOf("42" to property))
    }

    @Test
    fun `adds the deviceId and properties if the command map contains another deviceId`() {
        val scope = Scope(data = mutableMapOf(ControlDeviceAction.COMMAND_MAP to mutableMapOf("1337" to property)))

        action.execute(context, scope)

        assertThat(scope.data).containsKey(ControlDeviceAction.COMMAND_MAP)
        assertThat(scope.data[ControlDeviceAction.COMMAND_MAP]).isEqualTo(
            mutableMapOf(
                "42" to property,
                "1337" to property,
            )
        )
    }

    @Test
    fun `adds the properties if the command map contains the deviceId but with different properties`() {
        val scope = Scope(data = mutableMapOf(ControlDeviceAction.COMMAND_MAP to mutableMapOf("42" to propertyTwo)))

        action.execute(context, scope)

        assertThat(scope.data).containsKey(ControlDeviceAction.COMMAND_MAP)
        assertThat(scope.data[ControlDeviceAction.COMMAND_MAP]).isEqualTo(
            mutableMapOf(
                "42" to mutableMapOf(
                    "on" to SetBooleanValue(true),
                    "brightness" to IncrementNumberValue(1),
                )
            )
        )
    }

    @Test
    fun `overwrites existing properties if the command map contains the deviceId and properties`() {
        val scope = Scope(data = mutableMapOf(ControlDeviceAction.COMMAND_MAP to mutableMapOf("42" to propertyTwo)))

        action.execute(context, scope)

        assertThat(scope.data).containsKey(ControlDeviceAction.COMMAND_MAP)
        assertThat(scope.data[ControlDeviceAction.COMMAND_MAP]).isEqualTo(
            mutableMapOf(
                "42" to mutableMapOf(
                    "on" to SetBooleanValue(true),
                    "brightness" to IncrementNumberValue(1),
                )
            )
        )
    }

    @Test
    fun `overwrites existing properties and adds new properties`() {
        val scope = Scope(data = mutableMapOf(ControlDeviceAction.COMMAND_MAP to mutableMapOf("42" to property)))

        val action = ControlDeviceAction("42", propertyTwo + propertyThree)
        action.execute(context, scope)

        assertThat(scope.data).containsKey(ControlDeviceAction.COMMAND_MAP)
        assertThat(scope.data[ControlDeviceAction.COMMAND_MAP]).isEqualTo(
            mutableMapOf(
                "42" to mutableMapOf(
                    "on" to SetBooleanValue(false),
                    "brightness" to IncrementNumberValue(1),
                )
            )
        )
    }
}
