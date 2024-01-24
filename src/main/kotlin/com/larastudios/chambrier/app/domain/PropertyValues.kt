package com.larastudios.chambrier.app.domain

interface PropertyValue
interface AbsolutePropertyValue : PropertyValue
interface RelativePropertyValue : PropertyValue

data class SetBooleanValue(
    val value: Boolean,
) : AbsolutePropertyValue

data object ToggleBooleanValue : RelativePropertyValue

data class SetNumberValue(
    val value: Number,
) : AbsolutePropertyValue

data class IncrementNumberValue(
    val value: Number,
) : RelativePropertyValue

data class DecrementNumberValue(
    val value: Number,
) : RelativePropertyValue

data class SetColorValue(
    val xy: CartesianCoordinate,
    val gamut: Gamut?,
) : AbsolutePropertyValue

data class SetEnumValue<T: Enum<T>>(
    val value: T,
) : AbsolutePropertyValue
