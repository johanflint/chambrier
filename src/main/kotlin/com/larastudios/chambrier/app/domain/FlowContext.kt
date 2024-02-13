package com.larastudios.chambrier.app.domain

import com.larastudios.chambrier.app.flowEngine.Context
import kotlinx.coroutines.channels.SendChannel

data class FlowContext(
    val state: State,
    val commandChannel: SendChannel<List<DeviceCommand>>,
) : Context
