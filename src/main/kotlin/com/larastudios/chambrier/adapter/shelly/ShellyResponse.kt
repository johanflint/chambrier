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

data class WebhookList(val hooks: List<Webhook>)

data class Webhook(
    val id: Int,
    val cid: Int,
    val enable: Boolean,
    val event: String,
    val name: String,
    val urls: List<String>,
)

data class WebhookCreateRequestBody(
    val cid: Int,
    val event: String,
    val enable: Boolean = true,
    val name: String?,
    val urls: List<String>,
)

data class WebhookCreatedResponse(val id: Int, val rev: Int)
