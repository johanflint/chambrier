package com.larastudios.chambrier.adapter.shelly

import com.fasterxml.jackson.annotation.JsonProperty

data class GetDeviceInfo(
    val id: String,
    val name: String,
    val mac: String,
    val model: String,
    val gen: Int,
    val ver: String,
)

data class GetStatus(
    @JsonProperty("switch:0")
    val switch0: SwitchStatus,
)

data class SwitchStatus(
    val id: Int,
    val output: Boolean,
)
