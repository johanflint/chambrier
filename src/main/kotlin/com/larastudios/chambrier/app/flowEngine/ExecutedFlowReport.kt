package com.larastudios.chambrier.app.flowEngine

import java.time.Duration

data class ExecutedFlowReport(
    val scope: Map<String, Any>,
    val duration: Duration,
)
