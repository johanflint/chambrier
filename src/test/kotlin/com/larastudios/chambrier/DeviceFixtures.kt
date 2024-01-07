package com.larastudios.chambrier

import com.larastudios.chambrier.app.domain.Device
import com.larastudios.chambrier.app.domain.DeviceType

val lightDevice = Device(
    "90bdce60-3704-470e-be4c-8264f2bc8151",
    DeviceType.Light,
    "Signify Netherlands B.V.",
    "LWA021",
    "Hue filament bulb",
    "Livingroom",
    mapOf())

val lightDevice2 = Device(
    "39e84c3a-be8e-4eac-88dc-48baa2ab271d",
    DeviceType.Light,
    "Signify Netherlands B.V.",
    "LOM001",
    "Hue Smart plug",
    "Plug",
    mapOf()
)

val renamedLightDevice = lightDevice.copy(name = "Bedroom")
