package com.larastudios.chambrier.app.flowEngine

import com.larastudios.chambrier.app.domain.FlowContext
import com.larastudios.chambrier.app.domain.PropertyValue
import com.larastudios.chambrier.app.flowEngine.expression.Expression
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


interface Action {
    fun execute(context: Context, scope: Scope)
}

data object DoNothingAction : Action {
    override fun execute(context: Context, scope: Scope) {}
}

data class LogAction(val message: String) : Action {
    override fun execute(context: Context, scope: Scope) {
        logger.info { message }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

data class ControlDeviceAction(val deviceId: String, val propertyMap: Map<String, PropertyValue>): Action {
    override fun execute(context: Context, scope: Scope) {
        val state = (context as FlowContext).state
        val device = state.devices[deviceId]
        if (device == null) {
            logger.warn { "Unable to control unknown device '$deviceId', ignoring control device action: $propertyMap" }
            return
        }
        val filteredPropertyMap = propertyMap.filterKeys {
            val existingProperty = device.properties.containsKey(it)
            if (!existingProperty) {
                logger.warn { "Unable to set value of unknown property '$it' for device '${device.id}' (${device.name})" }
            }
            existingProperty
        }
        if (filteredPropertyMap.isEmpty()) {
            return
        }

        val commandMap: MutableMap<String, Map<String, PropertyValue>> = getCommandMap(scope.data) ?: mutableMapOf()
        commandMap.compute(deviceId) { _, currentValue ->
            currentValue?.plus(filteredPropertyMap) ?: filteredPropertyMap
        }

        scope.data[COMMAND_MAP] = commandMap
    }

    @Suppress("UNCHECKED_CAST")
    private fun getCommandMap(data: MutableMap<String, Any>): MutableMap<String, Map<String, PropertyValue>>? = data[COMMAND_MAP] as? MutableMap<String, Map<String, PropertyValue>>

    companion object {
        private val logger = KotlinLogging.logger {}
        const val COMMAND_MAP = "_commandMap"
    }
}
