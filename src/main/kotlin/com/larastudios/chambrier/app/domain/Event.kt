package com.larastudios.chambrier.app.domain

interface Event

data class DiscoveredDevices(val devices: List<Device>) : Event

interface PropertyChangedEvent : Event {
    val deviceId: String
    val propertyId: String
}

data class BooleanPropertyChanged(
    override val deviceId: String,
    override val propertyId: String,
    val value: Boolean,
) : PropertyChangedEvent

data class NumberPropertyChanged(
    override val deviceId: String,
    override val propertyId: String,
    val value: Number,
) : PropertyChangedEvent

data class ColorPropertyChanged(
    override val deviceId: String,
    override val propertyId: String,
    val xy: CartesianCoordinate,
    val gamut: Gamut?,
) : PropertyChangedEvent

data class EnumPropertyChanged<T : Enum<T>>(
    val deviceId: String,
    val propertyId: String,
    val value: T,
) : Event
