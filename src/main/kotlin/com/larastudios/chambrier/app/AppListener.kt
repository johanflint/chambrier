package com.larastudios.chambrier.app

import com.larastudios.chambrier.app.domain.ControlDeviceCommand
import com.larastudios.chambrier.app.domain.DeviceCommand
import com.larastudios.chambrier.app.domain.FlowContext
import com.larastudios.chambrier.app.flowEngine.ControlDeviceAction
import com.larastudios.chambrier.app.flowEngine.FlowEngine
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class AppListener(
    val observers: List<Observer>,
    val controllers: List<Controller>,
    val store: Store,
    val flowLoader: FlowLoader,
    val flowEngine: FlowEngine
) {
    @EventListener
    suspend fun handleContextRefreshEvent(event: ContextRefreshedEvent): Unit = coroutineScope {
        val flows = flowLoader.load()
        val commandChannel = Channel<List<DeviceCommand>>()

        launch(CoroutineName("commandChannel")) {
            commandChannel.consumeEach { commands ->
                logger.debug { "[commandChannel] Received ${commands.joinToString(", ")}" }
                controllers.forEach {
                    it.send(commands)
                }
            }
        }

        launch(CoroutineName("storeListener")) {
            store.state()
                .collect { state ->
                    logger.info { "[storeListener] $state" }
                    flows.map { flowEngine.execute(it, FlowContext(state, commandChannel)) }
                        .map {
                            getCommandMap(it.scope)
                        }
                        .fold(mapOf(), ::mergeCommandMaps)
                        .map { (deviceId, propertyMap) ->
                            val device = state.devices[deviceId] ?: throw IllegalArgumentException("State contains no device with id '$deviceId'")
                            ControlDeviceCommand(device, propertyMap)
                        }

                        if (commands.isNotEmpty()) {
                            commandChannel.send(commands)
                        }
                }
        }

        val eventFlow = observers.map {
            logger.info { "Starting ${it::class.simpleName}" }
            it.observe()
        }.merge()
        store.subscribe(eventFlow)

        logger.info { "Store subscribed to event stream" }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

@Suppress("UNCHECKED_CAST")
private fun getCommandMap(data: Map<String, Any>): CommandMap = data[ControlDeviceAction.COMMAND_MAP] as? CommandMap ?: mapOf()
