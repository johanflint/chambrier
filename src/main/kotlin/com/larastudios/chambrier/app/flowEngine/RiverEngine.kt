package com.larastudios.chambrier.app.flowEngine

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class RiverEngine : FlowEngine {
    override suspend fun execute(flow: Flow, context: Context): ExecutedFlowReport {
        logger.debug { "Executing flow ${flow.name}..." }
        val start = System.currentTimeMillis()

        val scope = Scope(mutableMapOf())
        executeNode(flow.startNode, context, scope)

        val durationInMs = System.currentTimeMillis() - start
        logger.debug { "Executing flow ${flow.name}... OK, took ${durationInMs}ms" }
        return ExecutedFlowReport(scope.data, Duration.ofMillis(durationInMs))
    }

    private tailrec suspend fun executeNode(node: FlowNode, context: Context, scope: Scope) {
        val nextNode = when (node) {
            is ActionFlowNode -> {
                logger.debug { "Executing action ${node.action::class.simpleName}" }
                node.action.execute(context, scope)
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
            executeNode(nextNode, context, scope)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

class NoAcceptedOutgoingNodeException(override val message: String) : Exception(message)
