package com.larastudios.chambrier.app.flowEngine

import com.larastudios.chambrier.app.flowEngine.expression.Expression

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
    val value: Any? = null
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

data class ConditionalFlowNode(
    override val id: String,
    override val outgoingNodes: List<FlowLink>,
    val condition: Expression
) : FlowNode

data class ActionFlowNode(
    override val id: String,
    override val outgoingNodes: List<FlowLink>,
    val action: Action
) : FlowNode
