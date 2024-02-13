package com.larastudios.chambrier.app.flowEngine

import com.larastudios.chambrier.app.domain.ControlDeviceCommand
import com.larastudios.chambrier.app.domain.DeviceCommand
import com.larastudios.chambrier.app.domain.FlowContext
import com.larastudios.chambrier.app.domain.SetBooleanValue
import com.larastudios.chambrier.initialState
import com.larastudios.chambrier.lightDevice
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("SendDeviceCommandsAction")
class SendDeviceCommandsActionTest {
    private val property = mapOf("on" to SetBooleanValue(true))
    private val scope = Scope(mutableMapOf(ControlDeviceAction.COMMAND_MAP to mutableMapOf(lightDevice.id to property)))
    private val channel = Channel<List<DeviceCommand>>()

    @Test
    fun `sends the command map to the channel`() = runTest {
        val context = FlowContext(initialState, channel)

        launch {
            SendDeviceCommandsAction.execute(context, scope)
        }

        val command = channel.receive()
        assertThat(command).isEqualTo(listOf(ControlDeviceCommand(lightDevice, property)))
    }

    @Test
    fun `clears command map from the scope`() = runTest {
        val context = FlowContext(initialState, channel)

        launch {
            SendDeviceCommandsAction.execute(context, scope)
        }

        channel.receive() // wait until the action is done
        assertThat(scope.data).isEmpty()
    }
}
