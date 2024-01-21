package com.larastudios.chambrier.app

import com.larastudios.chambrier.app.domain.DiscoveredDevices
import com.larastudios.chambrier.app.domain.Event
import com.larastudios.chambrier.app.domain.State
import com.larastudios.chambrier.lightDevice
import com.larastudios.chambrier.lightDevice2
import com.larastudios.chambrier.renamedLightDevice
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("DiscoveredDevicesReducer")
class DiscoveredDevicesReducerTest {
    private val initialState = State(mapOf(lightDevice.id to lightDevice))

    @Test
    fun `reduces DiscoveredDevicesReducerTest by adding all devices`() {
        val event = DiscoveredDevices(listOf(lightDevice2))
        val newState = DiscoveredDevicesReducer().reduce(event, initialState)

        assertThat(newState.devices)
            .containsEntry(lightDevice.id, lightDevice)
            .containsEntry(lightDevice2.id, lightDevice2)
            .hasSize(2)
    }

    @Test
    fun `reduces DiscoveredDevicesReducerTest by replacing an existing device`() {
        val event = DiscoveredDevices(listOf(renamedLightDevice))
        val newState = DiscoveredDevicesReducer().reduce(event, initialState)

        assertThat(newState.devices)
            .containsEntry(lightDevice.id, renamedLightDevice)
            .hasSize(1)
    }

    @Test
    fun `ignores other events by returning the unmodified state`() {
        val event = mockk<Event>()
        val newState = DiscoveredDevicesReducer().reduce(event, initialState)

        assertThat(newState).isEqualTo(initialState)
    }
}
