package com.larastudios.chambrier.app.flowEngine

interface FlowEngine {
    fun execute(flow: Flow, context: Context): ExecutedFlowReport
}
