package com.larastudios.chambrier.app.domain

import arrow.optics.optics

@optics
data class State(
    val devices: Map<String, Device>
) {
    companion object
}
