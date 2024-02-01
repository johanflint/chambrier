package com.larastudios.chambrier.adapter.hue.sse

import com.larastudios.chambrier.adapter.hue.mirekToKelvin
import com.larastudios.chambrier.app.domain.*

fun mapChangedLightProperty(property: ChangedLightProperty): Sequence<Event> = sequence {
    if (property.on != null) {
        yield(BooleanPropertyChanged(property.owner.rid, "on", property.on.on))
    }

    if (property.dimming != null) {
        yield(NumberPropertyChanged(property.owner.rid, "brightness", property.dimming.brightness))
    }

    if (property.color != null) {
        val xy = CartesianCoordinate(property.color.xy.x, property.color.xy.y)
        val gamut = property.color.gamut?.let {
            Gamut(
                red = CartesianCoordinate(it.red.x, it.red.y),
                green = CartesianCoordinate(it.green.x, it.green.y),
                blue = CartesianCoordinate(it.blue.x, it.blue.y),
            )
        }
        yield(ColorPropertyChanged(property.owner.rid, "color", xy, gamut))
    }

    if (property.colorTemperature != null) {
        yield(NumberPropertyChanged(property.owner.rid, "colorTemperature", mirekToKelvin(property.colorTemperature.mirek)))
    }
}
