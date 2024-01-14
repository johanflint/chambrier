package com.larastudios.chambrier.adapter.hue

import com.larastudios.chambrier.app.ObservationException
import com.larastudios.chambrier.app.domain.Device
import com.larastudios.chambrier.app.domain.DeviceType

fun mapLights(lights: List<LightGet>, deviceMap: Map<String, DeviceGet>): List<Device> =
    lights.map { light ->
        val deviceGet = deviceMap[light.owner.rid] ?: throw ObservationException("")

        Device(
            deviceGet.id,
            DeviceType.Light,
            deviceGet.productData.manufacturerName,
            deviceGet.productData.modelId,
            deviceGet.productData.productName,
            deviceGet.metadata.name,
            mapOf()
        )
    }
