package com.larastudios.chambrier.adapter.hue.sse

import java.time.LocalDateTime

data class SseData(
    val id: String,
    val type: SseDataType,
    val creationtime: LocalDateTime,
    val data: List<ChangedProperty>,
)

@Suppress("unused")
enum class SseDataType {
    Add,
    Update,
    Delete,
    Error
}

interface ChangedProperty {
    val id: String
}

data class UnknownChangedProperty(
    override val id: String,
    val type: String,
    val payload: String,
) : ChangedProperty
