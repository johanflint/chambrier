package com.larastudios.chambrier.adapter.hue

/**
 * Like [LightGet], but [ChangedLightProperty.on] is nullable.
 */
data class ChangedLightProperty(
    override val id: String,
    val owner: Owner,
    val on: On?,
    val dimming: Dimming?,
    val colorTemperature: ChangedColorTemperature?,
    val color: ChangedColor?,
) : ChangedProperty

/**
 * Like [ColorTemperature], but [ColorTemperature.mirekSchema] is absent.
 */
data class ChangedColorTemperature(
    val mirek: Int,
    val mirekValid: Boolean,
)

/**
 * Like [Color], but [Color.gamutType] is absent.
 */
data class ChangedColor(
    val xy: Xy,
    val gamut: ColorGamut?,
)
