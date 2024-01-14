package com.larastudios.chambrier.adapter.hue

import com.larastudios.chambrier.app.ObservationException
import com.larastudios.chambrier.app.domain.BooleanProperty
import com.larastudios.chambrier.app.domain.Device
import com.larastudios.chambrier.app.domain.DeviceType
import com.larastudios.chambrier.app.domain.PropertyType

fun mapLights(lights: List<LightGet>, deviceMap: Map<String, DeviceGet>): List<Device> =
    lights.map { light ->
        val deviceGet = deviceMap[light.owner.rid] ?: throw ObservationException("")

        val onProperty = BooleanProperty(
            "on",
            PropertyType.On,
            readonly = false,
            value = light.on.on,
            externalId = light.id
        )

        Device(
            deviceGet.id,
            DeviceType.Light,
            deviceGet.productData.manufacturerName,
            deviceGet.productData.modelId,
            deviceGet.productData.productName,
            deviceGet.metadata.name,
            mapOf(
                onProperty.name to onProperty
            )
        )
    }
