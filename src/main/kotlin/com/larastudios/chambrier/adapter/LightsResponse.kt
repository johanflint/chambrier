package com.larastudios.chambrier.adapter

data class LightsResponse(
    val data: List<LightGet>,
    val errors: List<HueError>,
)

data class LightGet(
    val id: String,
    val owner: Owner,
    val on: On,
    val dimming: Dimming?,
)

data class Owner(
    val rid: String,
    val rtype: String,
)

data class On(val on: Boolean)

data class Dimming(
    val brightness: Int, // >= 0 && <= 100
    val minDimLevel: Int?, // >= 0 && <= 100
)
