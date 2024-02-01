package com.larastudios.chambrier.app.domain

import com.larastudios.chambrier.app.flowEngine.Context

data class FlowContext(
    val state: State
) : Context
