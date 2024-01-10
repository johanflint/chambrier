package com.larastudios.chambrier.app.flowEngine

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class RiverEngine : FlowEngine {
    override fun execute(flow: Flow) {
        logger.debug { "Executing flow ${flow.name}..." }
        executeNode(flow.startNode)
        logger.debug { "Executing flow ${flow.name}... OK" }
    }

    private tailrec fun executeNode(node: FlowNode) {
        if (node is ActionFlowNode) {
            logger.debug { "Executing action ${node.action::class.simpleName}" }
            node.action.execute()
        }

        val nextNode = node.outgoingNodes.first().node
        if (nextNode !is EndFlowNode) {
            executeNode(nextNode)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
