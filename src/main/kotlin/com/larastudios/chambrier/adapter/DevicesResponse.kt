package com.larastudios.chambrier.adapter

data class DevicesResponse(
    val data: List<DeviceGet>,
    val errors: List<HueError>,
)

data class DeviceGet(
    val id: String,
    val productData: ProductData,
)

data class ProductData(
    val modelId: String,
    val manufacturerName: String,
    val productName: String,
    val productArchetype: String,
    val certified: Boolean,
    val softwareVersion: String, // pattern: \d+\.\d+\.\d+
    val hardwarePlatformType: String?,
)
