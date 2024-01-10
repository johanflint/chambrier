package com.larastudios.chambrier.app.flowEngine

import io.github.oshai.kotlinlogging.KotlinLogging

data class Flow(
    val name: String,
    val nodes: List<FlowNode>,
    val startNode: StartFlowNode
)

interface FlowNode {
    val id: String
    val outgoingNodes: List<FlowLink>
}

data class FlowLink(
    val node: FlowNode,
    val value: String? = null
)

data class StartFlowNode(
    override val id: String,
    override val outgoingNodes: List<FlowLink>
) : FlowNode

data class EndFlowNode(
    override val id: String,
) : FlowNode {
    override val outgoingNodes: List<FlowLink> = listOf()
}

data class ActionFlowNode(
    override val id: String,
    override val outgoingNodes: List<FlowLink>,
    val action: Action
) : FlowNode


interface Action {
    fun execute()
}

data object DoNothingAction : Action {
    override fun execute() {}
}

data class LogAction(val message: String) : Action {
    override fun execute() {
        logger.info { message }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
