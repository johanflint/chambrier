package com.larastudios.chambrier.app

import com.larastudios.chambrier.app.domain.DiscoveredDevices
import com.larastudios.chambrier.app.domain.Event
import com.larastudios.chambrier.app.domain.Reducer
import com.larastudios.chambrier.app.domain.State
import org.springframework.stereotype.Component

@Component
class DiscoveredDevicesReducer : Reducer {
    override fun reduce(event: Event, state: State): State {
        if (event is DiscoveredDevices) {
            val newDevices = event.devices.associateBy { it.id }
            return state.copy(devices = state.devices + newDevices)
        }

        return state
    }
}
