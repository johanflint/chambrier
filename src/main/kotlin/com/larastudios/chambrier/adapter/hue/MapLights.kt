package com.larastudios.chambrier.adapter.hue

import com.larastudios.chambrier.app.ObservationException
import com.larastudios.chambrier.app.domain.*
import com.larastudios.chambrier.app.domain.Unit

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

        val brightnessProperty = light.dimming?.let {
            NumberProperty(
                "brightness",
                PropertyType.Brightness,
                readonly = false,
                unit = Unit.Percentage,
                value = it.brightness,
                minimum = it.minDimLevel ?: 0,
                maximum = 100,
            )
        }

        val colorProperty = light.color?.let {
            ColorProperty(
                "color",
                PropertyType.Color,
                readonly = false,
                xy = CartesianCoordinate(it.xy.x, it.xy.y),
                gamut = it.gamut?.let {
                    Gamut(
                        red = CartesianCoordinate(it.red.x, it.red.y),
                        green = CartesianCoordinate(it.green.x, it.green.y),
                        blue = CartesianCoordinate(it.blue.x, it.blue.y),
                    )
                }
            )
        }

        val properties = listOfNotNull(onProperty, brightnessProperty, colorProperty).associateBy { it.name }
        Device(
            deviceGet.id,
            DeviceType.Light,
            deviceGet.productData.manufacturerName,
            deviceGet.productData.modelId,
            deviceGet.productData.productName,
            deviceGet.metadata.name,
            properties,
        )
    }
