package com.larastudios.chambrier.app.domain

interface Property {
    val name: String
    val type: PropertyType
    val readonly: Boolean
    val externalId: String?
}

enum class PropertyType {
    On,
}

data class BooleanProperty(
    override val name: String,
    override val type: PropertyType,
    override val readonly: Boolean,
    val value: Boolean,
    override val externalId: String? = null,
) : Property
