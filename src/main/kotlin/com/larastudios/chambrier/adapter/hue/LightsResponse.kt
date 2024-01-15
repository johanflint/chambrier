package com.larastudios.chambrier.adapter.hue

data class LightGet(
    val id: String,
    val owner: Owner,
    val on: On,
    val dimming: Dimming?,
    val color: Color?,
)

data class On(val on: Boolean)

data class Dimming(
    val brightness: Int, // >= 0 && <= 100
    val minDimLevel: Int?, // >= 0 && <= 100
)

data class Color(
    val xy: Xy,
    val gamut: ColorGamut?,
    val gamutType: String, // A, B, C
)

data class Xy(val x: Double, val y: Double) // >= 0 && <= 1

data class ColorGamut(val red: Xy, val green: Xy, val blue: Xy)
