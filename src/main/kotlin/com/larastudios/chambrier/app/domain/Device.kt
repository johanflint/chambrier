package com.larastudios.chambrier.app.domain

data class Device(
    val id: String,
    val type: DeviceType,
    val manufacturer: String,
    val modelId: String,
    val productName: String,
    val name: String,
    val properties: Map<String, Property>,
    val externalId: String? = null,
)

interface Property {
    val name: String
    val readonly: Boolean
    val externalId: String?
}

enum class DeviceType {
    Light,
    Switch,
}
