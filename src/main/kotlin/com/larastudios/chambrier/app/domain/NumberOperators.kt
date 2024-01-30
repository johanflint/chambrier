package com.larastudios.chambrier.app.domain

operator fun Number.plus(other: Number): Number = when (this) {
    is Int -> {
        when (other) {
            is Int -> this + other
            is Long -> this + other
            is Double -> this + other
            else -> throws()
        }
    }
    is Long -> {
        when (other) {
            is Int -> this + other
            is Long -> this + other
            is Double -> this + other
            else -> throws()
        }
    }
    is Double -> {
        when (other) {
            is Int -> this + other
            is Long -> this + other
            is Double -> this + other
            else -> throws()
        }
    }
    else -> throws()
}

operator fun Number.minus(other: Number): Number = when (this) {
    is Int -> {
        when (other) {
            is Int -> this - other
            is Long -> this - other
            is Double -> this - other
            else -> throws()
        }
    }
    is Long -> {
        when (other) {
            is Int -> this - other
            is Long -> this - other
            is Double -> this - other
            else -> throws()
        }
    }
    is Double -> {
        when (other) {
            is Int -> this - other
            is Long -> this - other
            is Double -> this - other
            else -> throws()
        }
    }
    else -> throws()
}

// Must have a different name than coerceIn to prevent recursive calls
fun Number.coerceInNullable(minimumValue: Number?, maximumValue: Number?): Number = when (this) {
    is Int -> this.coerceIn(minimumValue?.toInt(), maximumValue?.toInt())
    is Long -> this.coerceIn(minimumValue?.toLong(), maximumValue?.toLong())
    is Double -> this.coerceIn(minimumValue?.toDouble(), maximumValue?.toDouble())
    else -> throws()
}

@Suppress("NOTHING_TO_INLINE")
private inline fun throws(): Nothing = throw UnsupportedOperationException("Unsupported number type")
