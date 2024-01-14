package com.larastudios.chambrier.adapter

import java.time.LocalDate

data class ButtonResponse(
    val data: List<ButtonGet>,
    val errors: List<HueError>,
)

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
