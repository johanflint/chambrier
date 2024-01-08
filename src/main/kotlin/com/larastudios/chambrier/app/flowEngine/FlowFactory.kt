package com.larastudios.chambrier.app.flowEngine

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service

@Service
class FlowFactory(private val objectMapper: ObjectMapper) {
    fun fromJson(json: String): Flow {
        val serializedFlow = objectMapper.readValue<SerializedFlow>(json)

        serializedFlow.nodes.count { it.type == "startFlowNode" }.let { numStartNodes ->
            if (numStartNodes == 0) {
                throw MissingNodeException("No start node found")
            }
            if (numStartNodes > 1) {
                throw TooManyStartNodesException("Only one start node is allowed, found $numStartNodes")
            }
        }

        val endNodes = serializedFlow.nodes.filter { it.type == "endFlowNode" }
        if (endNodes.isEmpty()) {
            throw MissingNodeException("No end nodes found")
        }

        val nodesToVisit = ArrayDeque(endNodes)
        val flowNodeMap = mutableMapOf<String, FlowNode>()

        while (nodesToVisit.isNotEmpty()) {
            val serializedNode = nodesToVisit.removeLast()

            val incomingNodes = serializedFlow.nodes
                .filter { it.outgoingNode == serializedNode.id }

            if (serializedNode.type != "startFlowNode" && incomingNodes.isEmpty()) {
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
                ?: throw MissingNodeException("Node '${serializedNode.id}' has a missing outgoing node to '${serializedNode.outgoingNode}'")
            return listOf(FlowLink(node))
        }

        return listOf()
    }

    private fun SerializedFlowNode.toFlowNode(outgoingNodes: List<FlowLink>): FlowNode =
        when (type) {
            "startFlowNode" -> StartFlowNode(id, outgoingNodes)
            "endFlowNode" -> EndFlowNode(id)
            else -> throw UnknownNodeTypeException(type)
        }
}

class TooManyStartNodesException(override val message: String?) : Exception(message)
class NoConnectingNodeException(override val message: String?) : Exception(message)
class MissingNodeException(override val message: String?) : Exception(message)
class UnusedNodesException(override val message: String?) : Exception(message)
class UnknownNodeTypeException(override val message: String?) : Exception(message)
