package com.larastudios.chambrier.app.flowEngine

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.larastudios.chambrier.app.flowEngine.expression.Expression
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class FlowFactory(private val objectMapper: ObjectMapper, private val expressionDeserializer: ExpressionDeserializer) {
    fun fromJson(json: String): Flow {
        val serializedFlow = try {
            objectMapper
                .registerModule(SimpleModule().addDeserializer(Expression::class.java, expressionDeserializer))
                .readValue<SerializedFlow>(json)
        } catch (e: InvalidTypeIdException) {
            logger.error(e) { "Unknown node type" }
            throw UnknownNodeTypeException(e.typeId)
        }

        serializedFlow.nodes.count { it is SerializedStartFlowNode }.let { numStartNodes ->
            if (numStartNodes == 0) {
                throw MissingNodeException("No start node found")
            }
            if (numStartNodes > 1) {
                throw TooManyStartNodesException("Only one start node is allowed, found $numStartNodes")
            }
        }

        val endNodes = serializedFlow.nodes.filterIsInstance<SerializedEndFlowNode>()
        if (endNodes.isEmpty()) {
            throw MissingNodeException("No end nodes found")
        }

        val nodesToVisit = ArrayDeque<SerializedFlowNode>(endNodes)
        val flowNodeMap = mutableMapOf<String, FlowNode>()

        while (nodesToVisit.isNotEmpty()) {
            val serializedNode = nodesToVisit.removeLast()

            val incomingNodes = serializedFlow.nodes
                .filter { it.outgoingNode == serializedNode.id }

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
                ?: throw MissingNodeException("Node '${serializedNode.id}' has a missing outgoing node to '${serializedNode.outgoingNode}'")
            return listOf(FlowLink(node))
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
class MissingNodeException(override val message: String?) : Exception(message)
class UnusedNodesException(override val message: String?) : Exception(message)
class UnknownNodeTypeException(override val message: String?) : Exception(message)
class UnknownExpressionTypeException(override val message: String?) : Exception(message)
class UnknownActionTypeException(override val message: String?) : Exception(message)
