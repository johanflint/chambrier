package com.larastudios.chambrier.adapter.hue

import com.larastudios.chambrier.app.domain.Device
import com.larastudios.chambrier.app.domain.DeviceType

fun mapSwitches(buttons: List<ButtonGet>, deviceMap: Map<String, DeviceGet>): List<Device> {
    val switches = buttons.map { it.owner.rid }.toSet().mapNotNull { deviceMap[it] }
    return switches.map { deviceGet ->
        Device(
            deviceGet.id,
            DeviceType.Switch,
            deviceGet.productData.manufacturerName,
            deviceGet.productData.modelId,
            deviceGet.productData.productName,
            deviceGet.metadata.name,
            mapOf()
        )
    }
}
