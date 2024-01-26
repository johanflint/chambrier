package com.larastudios.chambrier.app.flowEngine

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class RiverEngine : FlowEngine {
    override fun execute(flow: Flow) {
        logger.debug { "Executing flow ${flow.name}..." }
        val start = System.currentTimeMillis()

        executeNode(flow.startNode, Scope(mutableMapOf()))

        val durationInMs = System.currentTimeMillis() - start
        logger.debug { "Executing flow ${flow.name}... OK, took ${durationInMs}ms" }
    }

    private tailrec fun executeNode(node: FlowNode, scope: Scope) {
        val nextNode = when (node) {
            is ActionFlowNode -> {
                logger.debug { "Executing action ${node.action::class.simpleName}" }
                node.action.execute(scope)
                node.outgoingNodes.first().node
            }
            is ConditionalFlowNode -> {
                val result = evaluateExpression(node.condition)
                logger.debug { "Expression ${node.condition.stringify()} evaluated to '$result' (${result::class})" }
                node.outgoingNodes.firstOrNull { it.value == result }
                    ?.node
                    ?: throw NoAcceptedOutgoingNodeException("Condition of node '${node.id}' evaluated to '$result', but no outgoing node has a matching value: ${node.outgoingNodes.joinToString(", ") { "${it.value}" }}")
            }
            else -> node.outgoingNodes.first().node
        }

        logger.debug { "Next node: ${nextNode.id}" }
        if (nextNode !is EndFlowNode) {
            executeNode(nextNode, scope)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

class NoAcceptedOutgoingNodeException(override val message: String) : Exception(message)
