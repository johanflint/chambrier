package com.larastudios.chambrier.adapter.hue

import com.larastudios.chambrier.app.domain.*

fun mapSwitches(buttons: List<ButtonGet>, deviceMap: Map<String, DeviceGet>): List<Device> {
    val switches = buttons.map { it.owner.rid }.toSet().mapNotNull { deviceMap[it] }
    return switches.map { deviceGet ->
        val properties = buttons.filter { it.owner.rid == deviceGet.id }
            .map {
                EnumProperty(
                    "button${it.metadata.controlId}",
                    PropertyType.Button,
                    readonly = true,
                    values = HueButtonState.entries.toList(),
                    value =  it.button.buttonReport?.event.toHueButtonState(),
                    externalId = it.id
                )
            }

        Device(
            deviceGet.id,
            DeviceType.Switch,
            deviceGet.productData.manufacturerName,
            deviceGet.productData.modelId,
            deviceGet.productData.productName,
            deviceGet.metadata.name,
            properties.associateBy { it.name }
        )
    }
}

fun String?.toHueButtonState(): HueButtonState {
    val value = this?.replace("_", "")?.lowercase()
    return HueButtonState.entries.firstOrNull { it.name.lowercase() == value } ?: HueButtonState.Unknown
}
