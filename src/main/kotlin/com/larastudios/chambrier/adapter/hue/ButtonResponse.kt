package com.larastudios.chambrier.adapter.hue

import java.time.LocalDate

data class ButtonGet(
    val id: String,
    val owner: Owner,
    val metadata: ButtonMetadata,
    val button: ButtonData,
)

data class ButtonMetadata(
    val controlId: Int
)

data class ButtonData(
    val buttonReport: ButtonReport?,
    val repeatInterval: Int,
    val eventValues: List<String>,
)

data class ButtonReport(
    val updated: LocalDate,
    val event: String,
)
