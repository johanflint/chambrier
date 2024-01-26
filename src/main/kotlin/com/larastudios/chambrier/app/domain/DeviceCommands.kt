package com.larastudios.chambrier.app.domain

interface DeviceCommand

data class ControlDeviceCommand(
    val deviceId: String,
    val propertyMap: Map<String, PropertyValue>
) : DeviceCommand
