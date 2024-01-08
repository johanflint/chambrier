package com.larastudios.chambrier.app.flowEngine

data class SerializedFlow(
    val name: String,
    val nodes: List<SerializedFlowNode>,
)

data class SerializedFlowNode(
    val id: String,
    val type: String,
    val outgoingNode: String?,
)
