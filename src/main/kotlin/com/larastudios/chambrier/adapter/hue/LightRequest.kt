package com.larastudios.chambrier.adapter.hue

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.*
import com.larastudios.chambrier.app.domain.CartesianCoordinate

@JsonInclude(Include.NON_NULL)
data class LightRequest(
    val on: On? = null,
    val dimming: SetDimming? = null,
    val colorTemperature: SetColorTemperature? = null,
    val color: SetColor? = null,
)

data class SetDimming(
    val brightness: Number, // >= 0 && <= 100
)

data class SetColorTemperature(
    val mirek: Int, // >= 153 && <= 500
)

data class SetColor(val xy: CartesianCoordinate)
