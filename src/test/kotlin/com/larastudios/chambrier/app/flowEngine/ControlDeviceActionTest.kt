package com.larastudios.chambrier.app.flowEngine

import com.larastudios.chambrier.app.domain.*
import com.larastudios.chambrier.initialState
import com.larastudios.chambrier.lightDevice
import com.larastudios.chambrier.lightDevice2
import com.larastudios.chambrier.switchDevice
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("ControlDeviceAction")
class ControlDeviceActionTest {
    private val property = mapOf("on" to SetBooleanValue(true))
    private val propertyTwo = mapOf("brightness" to IncrementNumberValue(1))
    private val propertyThree = mapOf("on" to SetBooleanValue(false))
    private val action = ControlDeviceAction(lightDevice.id, property)
    private val context = FlowContext(state = initialState, commandChannel = Channel())

    @Test
    fun `does not modify the scope if the device is unknown `() = runTest {
        val scope = Scope()

        action.execute(FlowContext(State(mapOf()), Channel()), scope)

        assertThat(scope.data).isEmpty()
    }

    @Test
    fun `ignores all properties if the device is known but all properties are unknown`() = runTest {
        val scope = Scope()

        val action = ControlDeviceAction(lightDevice.id, mapOf("unknownProperty" to SetBooleanValue(true)))
        action.execute(context, scope)

        assertThat(scope.data).isEmpty()
    }

    @Test
    fun `ignores valid properties that are read-only`() = runTest {
        val scope = Scope()

        val action = ControlDeviceAction(switchDevice.id, mapOf("button1" to SetEnumValue(HueButtonState.ShortRelease)))
        action.execute(context, scope)

        assertThat(scope.data).isEmpty()
    }

    @Test
    fun `adds the known properties while ignoring unknown properties`() = runTest {
        val scope = Scope()

        val action = ControlDeviceAction(lightDevice.id, mapOf("unknownProperty" to SetBooleanValue(true)) + property)
        action.execute(context, scope)

        assertThat(scope.data).containsKey(ControlDeviceAction.COMMAND_MAP)
        assertThat(scope.data[ControlDeviceAction.COMMAND_MAP]).isEqualTo(mutableMapOf(lightDevice.id to property))
    }

    @Test
    fun `adds the deviceId and properties if the command map is absent from the scope`() = runTest {
        val scope = Scope()

        action.execute(context, scope)

        assertThat(scope.data).containsKey(ControlDeviceAction.COMMAND_MAP)
        assertThat(scope.data[ControlDeviceAction.COMMAND_MAP]).isEqualTo(mutableMapOf(lightDevice.id to property))
    }

    @Test
    fun `adds the deviceId and properties if the command map contains another deviceId`() = runTest {
        val scope = Scope(data = mutableMapOf(ControlDeviceAction.COMMAND_MAP to mutableMapOf(lightDevice2.id to property)))

        action.execute(context, scope)

        assertThat(scope.data).containsKey(ControlDeviceAction.COMMAND_MAP)
        assertThat(scope.data[ControlDeviceAction.COMMAND_MAP]).isEqualTo(
            mutableMapOf(
                lightDevice.id to property,
                lightDevice2.id to property,
            )
        )
    }

    @Test
    fun `adds the properties if the command map contains the deviceId but with different properties`() = runTest {
        val scope = Scope(data = mutableMapOf(ControlDeviceAction.COMMAND_MAP to mutableMapOf(lightDevice.id to propertyTwo)))

        action.execute(context, scope)

        assertThat(scope.data).containsKey(ControlDeviceAction.COMMAND_MAP)
        assertThat(scope.data[ControlDeviceAction.COMMAND_MAP]).isEqualTo(
            mutableMapOf(
                lightDevice.id to mutableMapOf(
                    "on" to SetBooleanValue(true),
                    "brightness" to IncrementNumberValue(1),
                )
            )
        )
    }

    @Test
    fun `overwrites existing properties if the command map contains the deviceId and properties`() = runTest {
        val scope = Scope(data = mutableMapOf(ControlDeviceAction.COMMAND_MAP to mutableMapOf(lightDevice.id to propertyTwo)))

        action.execute(context, scope)

        assertThat(scope.data).containsKey(ControlDeviceAction.COMMAND_MAP)
        assertThat(scope.data[ControlDeviceAction.COMMAND_MAP]).isEqualTo(
            mutableMapOf(
                lightDevice.id to mutableMapOf(
                    "on" to SetBooleanValue(true),
                    "brightness" to IncrementNumberValue(1),
                )
            )
        )
    }

    @Test
    fun `overwrites existing properties and adds new properties`() = runTest {
        val scope = Scope(data = mutableMapOf(ControlDeviceAction.COMMAND_MAP to mutableMapOf(lightDevice.id to property)))

        val action = ControlDeviceAction(lightDevice.id, propertyTwo + propertyThree)
        action.execute(context, scope)

        assertThat(scope.data).containsKey(ControlDeviceAction.COMMAND_MAP)
        assertThat(scope.data[ControlDeviceAction.COMMAND_MAP]).isEqualTo(
            mutableMapOf(
                lightDevice.id to mutableMapOf(
                    "on" to SetBooleanValue(false),
                    "brightness" to IncrementNumberValue(1),
                )
            )
        )
    }
}
