package com.larastudios.chambrier.app.domain

sealed interface PropertyValue
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
) : AbsolutePropertyValue

data class SetEnumValue<T: Enum<T>>(
    val value: T,
) : AbsolutePropertyValue

fun PropertyValue.isAssignableTo(property: Property): Boolean = when (property) {
    is BooleanProperty -> this is SetBooleanValue || this is ToggleBooleanValue
    is NumberProperty -> this is SetNumberValue || this is IncrementNumberValue || this is DecrementNumberValue
    is ColorProperty -> this is SetColorValue
    is EnumProperty<*> -> this is SetEnumValue<*>
}
