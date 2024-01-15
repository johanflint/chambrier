package com.larastudios.chambrier.app.domain

interface Property {
    val name: String
    val type: PropertyType
    val readonly: Boolean
    val externalId: String?
}

enum class PropertyType {
    On,
    Brightness,
    Color,
    ColorTemperature,
}

data class BooleanProperty(
    override val name: String,
    override val type: PropertyType,
    override val readonly: Boolean,
    val value: Boolean,
    override val externalId: String? = null,
) : Property

data class NumberProperty(
    override val name: String,
    override val type: PropertyType,
    override val readonly: Boolean,
    val unit: Unit,
    val value: Number?,
    val minimum: Number?,
    val maximum: Number?,
    override val externalId: String? = null,
) : Property

enum class Unit(val symbol: String) {
    Percentage("%"),
    Lux("lx"),
    DegreesCelsius("°C"),
    Kelvin("K")
}

data class ColorProperty(
    override val name: String,
    override val type: PropertyType,
    override val readonly: Boolean,
    val xy: CartesianCoordinate,
    val gamut: Gamut?,
    override val externalId: String? = null,
) : Property

data class CartesianCoordinate(val x: Double, val y: Double)

data class Gamut(val red: CartesianCoordinate, val green: CartesianCoordinate, val blue: CartesianCoordinate)
