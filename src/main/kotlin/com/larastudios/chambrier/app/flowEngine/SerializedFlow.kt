package com.larastudios.chambrier.app.flowEngine

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class SerializedFlow(
    val name: String,
    val nodes: List<SerializedFlowNode>,
)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(SerializedStartFlowNode::class, name = "startFlowNode"),
    JsonSubTypes.Type(SerializedEndFlowNode::class, name = "endFlowNode"),
    JsonSubTypes.Type(SerializedActionFlowNode::class, name = "actionNode")
)
sealed interface SerializedFlowNode {
    val id: String
    val outgoingNode: String?
}

data class SerializedStartFlowNode(
    override val id: String,
    override val outgoingNode: String?,
) : SerializedFlowNode

data class SerializedEndFlowNode(
    override val id: String,
) : SerializedFlowNode {
    override val outgoingNode: String? = null
}

data class SerializedActionFlowNode(
    override val id: String,
    override val outgoingNode: String?,
    val action: SerializedAction
) : SerializedFlowNode

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(SerializedDoNothingAction::class, name = "doNothing"),
    JsonSubTypes.Type(SerializedLogAction::class, name = "log"),
)
interface SerializedAction

data object SerializedDoNothingAction : SerializedAction

data class SerializedLogAction(
    val message: String
) : SerializedAction
