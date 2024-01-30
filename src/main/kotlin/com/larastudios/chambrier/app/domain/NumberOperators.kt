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

@Suppress("NOTHING_TO_INLINE")
private inline fun throws(): Nothing = throw UnsupportedOperationException("Unsupported number type")
