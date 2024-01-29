package com.larastudios.chambrier

import com.larastudios.chambrier.app.domain.State

val initialState = State(
    devices = mapOf(
        lightDevice.id to lightDevice,
        lightDevice2.id to lightDevice2,
        switchDevice.id to switchDevice,
        editableSwitchDevice.id to editableSwitchDevice,
    )
)
