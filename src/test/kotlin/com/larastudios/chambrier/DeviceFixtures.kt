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
    mapOf(
        booleanProperty.name to booleanProperty,
        numberProperty.name to numberProperty,
        colorProperty.name to colorProperty,
    )
)

val lightDevice2 = Device(
    "39e84c3a-be8e-4eac-88dc-48baa2ab271d",
    DeviceType.Light,
    "Signify Netherlands B.V.",
    "LOM001",
    "Hue Smart plug",
    "Plug",
    mapOf(
        booleanProperty.name to booleanProperty,
        numberProperty.name to numberProperty,
        colorProperty.name to colorProperty,
    )
)

val renamedLightDevice = lightDevice.copy(name = "Bedroom")

val editableSwitchDevice = Device(
    "5f679dab-db82-42a7-9e2c-de8040373689",
    DeviceType.Switch,
    "Signify Netherlands B.V.",
    "RWL021",
    "Hue dimmer switch",
    "Kitchen",
    mapOf("button" to enumProperty)
)

val switchDevice = Device(
    "0a65707c-bf48-42e9-8eb5-9f9e5114cfa0",
    DeviceType.Switch,
    "Signify Netherlands B.V.",
    "RWL021",
    "Hue dimmer switch",
    "Kitchen",
    mapOf(
        "button1" to enumProperty.copy(name = "button1", readonly = true),
        "button2" to enumProperty.copy(name = "button2", readonly = true),
        "button3" to enumProperty.copy(name = "button3", readonly = true),
        "button4" to enumProperty.copy(name = "button4", readonly = true),
    )
)

