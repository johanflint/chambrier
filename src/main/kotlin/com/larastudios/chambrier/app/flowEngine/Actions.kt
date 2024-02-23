package com.larastudios.chambrier.app.flowEngine

import com.larastudios.chambrier.app.domain.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.time.delay
import java.time.Duration

interface Action {
    suspend fun execute(context: Context, scope: Scope)
}

data object DoNothingAction : Action {
    override suspend fun execute(context: Context, scope: Scope) {}
}

data class LogAction(val message: String) : Action {
    override suspend fun execute(context: Context, scope: Scope) {
        logger.info { message }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

data class WaitAction(val duration: Duration) : Action {
    override suspend fun execute(context: Context, scope: Scope) {
        delay(duration)
    }
}

data class ControlDeviceAction(val deviceId: String, val propertyMap: Map<String, PropertyValue>) : Action {
    override suspend fun execute(context: Context, scope: Scope) {
        val state = (context as FlowContext).state
        val device = state.devices[deviceId]
        if (device == null) {
            logger.warn { "Unable to control unknown device '$deviceId', ignoring control device action: $propertyMap" }
            return
        }
        val filteredPropertyMap = filterInvalidProperties(propertyMap, device)
        if (filteredPropertyMap.isEmpty()) {
            return
        }

        val commandMap: MutableMap<String, Map<String, PropertyValue>> = scope.commandMap() ?: mutableMapOf()
        commandMap.compute(deviceId) { _, currentValue ->
            currentValue?.plus(filteredPropertyMap) ?: filteredPropertyMap
        }

        scope.data[COMMAND_MAP] = commandMap
    }

    private fun filterInvalidProperties(propertyMap: Map<String, PropertyValue>, device: Device): Map<String, PropertyValue> =
        propertyMap.filter { (propertyId, propertyValue) ->
            val property = device.properties[propertyId]
            if (property == null) {
                logger.warn { "Unable to set value of unknown property '$propertyId' for device '${device.id}' (${device.name})" }
                false
            } else if (property.readonly) {
                logger.warn { "Unable to modify read-only property '$propertyId' for device '${device.id}' (${device.name})" }
                false
            } else {
                val isAssignable = propertyValue.isAssignableTo(property)
                if (!isAssignable) {
                    logger.warn { "Incompatible property types: property '$propertyId' of device '${device.id}' (${device.name}) is of type '${property::class.simpleName}', but the value is of type '${propertyValue::class.simpleName}'" }
                }
                isAssignable
            }
        }

    companion object {
        private val logger = KotlinLogging.logger {}
        const val COMMAND_MAP = "_commandMap"
    }
}

data object SendDeviceCommandsAction : Action {
    override suspend fun execute(context: Context, scope: Scope) {
        val commandMap: MutableMap<String, Map<String, PropertyValue>> = scope.commandMap() ?: return
        val flowContext = context as FlowContext
        val state = flowContext.state

        val commands = commandMap.map { (deviceId, propertyMap) ->
            val device = state.devices[deviceId] ?: throw IllegalArgumentException("State contains no device with id '$deviceId'")
            ControlDeviceCommand(device, propertyMap)
        }

        logger.info { "Commands: $commands" }

        context.commandChannel.send(commands)
        scope.data.remove(ControlDeviceAction.COMMAND_MAP)
    }

    private val logger = KotlinLogging.logger {}
}

@Suppress("UNCHECKED_CAST")
private fun Scope.commandMap(): MutableMap<String, Map<String, PropertyValue>>? = data[ControlDeviceAction.COMMAND_MAP] as? MutableMap<String, Map<String, PropertyValue>>
