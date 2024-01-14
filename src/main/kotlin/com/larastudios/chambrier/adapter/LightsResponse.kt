package com.larastudios.chambrier.adapter

data class LightGet(
    val id: String,
    val owner: Owner,
    val on: On,
    val dimming: Dimming?,
)

data class On(val on: Boolean)

data class Dimming(
    val brightness: Int, // >= 0 && <= 100
    val minDimLevel: Int?, // >= 0 && <= 100
)
