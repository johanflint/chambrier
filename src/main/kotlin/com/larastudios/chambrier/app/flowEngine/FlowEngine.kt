package com.larastudios.chambrier.app.flowEngine

interface FlowEngine {
    suspend fun execute(flow: Flow, context: Context): ExecutedFlowReport
}
