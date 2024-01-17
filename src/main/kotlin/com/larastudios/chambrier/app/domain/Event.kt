package com.larastudios.chambrier.app.domain

interface Event

data class DiscoveredDevices(val devices: List<Device>) : Event

data class BooleanPropertyChanged(
    val deviceId: String,
    val propertyId: String,
    val value: Boolean,
) : Event

data class NumberPropertyChanged(
    val deviceId: String,
    val propertyId: String,
    val value: Number,
) : Event

data class ColorPropertyChanged(
    val deviceId: String,
    val propertyId: String,
    val xy: CartesianCoordinate,
    val gamut: Gamut?,
) : Event

data class EnumPropertyChanged<T : Enum<T>>(
    val deviceId: String,
    val propertyId: String,
    val value: T,
) : Event
