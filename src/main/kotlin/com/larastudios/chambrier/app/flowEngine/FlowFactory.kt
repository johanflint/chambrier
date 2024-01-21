package com.larastudios.chambrier.app.flowEngine

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class FlowFactory(private val objectMapper: ObjectMapper) {
    fun fromJson(json: String): Flow {
        val serializedFlow = try {
            objectMapper.readValue<SerializedFlow>(json)
        } catch (e: InvalidTypeIdException) {
            logger.error(e) { "Unknown node type" }
            throw UnknownNodeTypeException(e.typeId)
        }

        serializedFlow.nodes.count { it is SerializedStartFlowNode }.let { numStartNodes ->
            if (numStartNodes == 0) {
                throw MissingStartNodeException()
            }
            if (numStartNodes > 1) {
                throw TooManyStartNodesException("Only one start node is allowed, found $numStartNodes")
            }
        }

        val endNodes = serializedFlow.nodes.filterIsInstance<SerializedEndFlowNode>()
        if (endNodes.isEmpty()) {
            throw MissingEndNodeException()
        }

        val nodesToVisit = ArrayDeque<SerializedFlowNode>(endNodes)
        val flowNodeMap = mutableMapOf<String, FlowNode>()

        while (nodesToVisit.isNotEmpty()) {
            val serializedNode = nodesToVisit.removeLast()

            val incomingNodes = serializedFlow.nodes
                .filter { it.outgoingNode == serializedNode.id || it is SerializedConditionalFlowNode && it.outgoingNodes.any { it.node == serializedNode.id } }

            if (serializedNode !is SerializedStartFlowNode && incomingNodes.isEmpty()) {
                throw NoConnectingNodeException("No links found to node '${serializedNode.id}' in flow '${serializedFlow.name}'")
            }

            nodesToVisit.addAll(0, incomingNodes)
            incomingNodes.forEach(nodesToVisit::addFirst)

            val outgoingNodes = mapOutgoingNodes(serializedNode, flowNodeMap)
            val node = serializedNode.toFlowNode(outgoingNodes)

            flowNodeMap[node.id] = node
        }

        if (serializedFlow.nodes.size > flowNodeMap.size) {
            val unusedNodeIds = serializedFlow.nodes.map { it.id }.subtract(flowNodeMap.keys)
            throw UnusedNodesException("Unused nodes: ${unusedNodeIds.joinToString(", ")}")
        }

        return Flow(serializedFlow.name, flowNodeMap.values.toList(), flowNodeMap.values.filterIsInstance<StartFlowNode>().first())
    }

    private fun mapOutgoingNodes(
        serializedNode: SerializedFlowNode,
        flowNodeMap: Map<String, FlowNode>
    ): List<FlowLink> {
        if (serializedNode.outgoingNode != null) {
            val node = flowNodeMap[serializedNode.outgoingNode]
                ?: throw MissingNodeException(serializedNode.id, "${serializedNode.outgoingNode}")
            return listOf(FlowLink(node))
        }
        if (serializedNode is SerializedConditionalFlowNode) {
            return serializedNode.outgoingNodes.map {
                val node = flowNodeMap[it.node]
                    ?: throw MissingNodeException(serializedNode.id, it.node)
                val value = it.value ?: throw FlowLinkNullValueException("Flow link from node '${serializedNode.id}' to node '${it.node}' has value null")
                FlowLink(node, value)
            }
        }

        return listOf()
    }

    private fun SerializedFlowNode.toFlowNode(outgoingNodes: List<FlowLink>): FlowNode =
        when (this) {
            is SerializedStartFlowNode -> StartFlowNode(id, outgoingNodes)
            is SerializedEndFlowNode -> EndFlowNode(id)
            is SerializedConditionalFlowNode -> ConditionalFlowNode(id, outgoingNodes, condition)
            is SerializedActionFlowNode -> {
                val action = when (this.action) {
                    is SerializedDoNothingAction -> DoNothingAction
                    is SerializedLogAction -> LogAction(this.action.message)
                    else -> throw UnknownActionTypeException(this::class.simpleName)
                }

                ActionFlowNode(id, outgoingNodes, action)
            }
        }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

class TooManyStartNodesException(override val message: String?) : Exception(message)
class NoConnectingNodeException(override val message: String?) : Exception(message)
class MissingNodeException(nodeId: String, outgoingNodeId: String) : Exception("Node '$nodeId' has a missing outgoing node to '$outgoingNodeId'")
class MissingStartNodeException : Exception()
class MissingEndNodeException : Exception()
class UnusedNodesException(override val message: String?) : Exception(message)
class UnknownNodeTypeException(override val message: String?) : Exception(message)
class UnknownExpressionTypeException(override val message: String?) : Exception(message)
class UnknownActionTypeException(override val message: String?) : Exception(message)
class FlowLinkNullValueException(override val message: String?) : Exception(message)
