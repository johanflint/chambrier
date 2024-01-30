package com.larastudios.chambrier.app.domain

interface DeviceCommand

data class ControlDeviceCommand(
    val device: Device,
    val propertyMap: Map<String, PropertyValue>
) : DeviceCommand
