package com.larastudios.chambrier.adapter.hue

/**
 * Like [ButtonGet], but [ButtonGet.metadata] is absent.
 */
data class ChangedButtonProperty(
    override val id: String,
    val owner: Owner,
    val button: ChangedButtonData,
) : ChangedProperty

/**
 * Like [ButtonData], but [ButtonData.repeatInterval] and [ButtonData.eventValues] are absent.
 */
data class ChangedButtonData(
    val buttonReport: ButtonReport
)
