package com.larastudios.chambrier.app

import com.larastudios.chambrier.app.flowEngine.Flow

interface FlowLoader {
    fun load(): List<Flow>
}

class FlowLoadException(override val message: String?) : Exception(message)
