package com.larastudios.chambrier.adapter.hue

data class HueResponse<T>(
    val data: List<T>,
    val errors: List<HueError>,
)

data class HueError(val description: String)

data class Owner(
    val rid: String,
    val rtype: String,
)
