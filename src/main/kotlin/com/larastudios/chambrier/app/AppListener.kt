package com.larastudios.chambrier.app

import com.larastudios.chambrier.app.domain.ControlDeviceCommand
import com.larastudios.chambrier.app.domain.FlowContext
import com.larastudios.chambrier.app.flowEngine.ControlDeviceAction
import com.larastudios.chambrier.app.flowEngine.FlowEngine
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.asFlux
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

        launch(CoroutineName("storeListener")) {
            store.state()
                .log()
                .asFlow()
                .collect { state ->
                    val commands = flows.map { flowEngine.execute(it, FlowContext(state)) }
                        .map {
                            getCommandMap(it.scope)
                        }
                        .fold(mapOf(), ::mergeCommandMaps)
                        .map { (deviceId, propertyMap) ->
                            val device = state.devices[deviceId] ?: throw IllegalArgumentException("State contains no device with id '$deviceId'")
                            ControlDeviceCommand(device, propertyMap)
                        }

                    controllers.forEach {
                        it.send(commands)
                    }
                }
        }

        val eventFlow = observers.map {
            logger.info { "Starting ${it::class.simpleName}" }
            it.observe()
        }.merge()
        store.subscribe(eventFlow.asFlux())

        logger.info { "Store subscribed to event stream" }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

@Suppress("UNCHECKED_CAST")
private fun getCommandMap(data: Map<String, Any>): CommandMap = data[ControlDeviceAction.COMMAND_MAP] as? CommandMap ?: mapOf()
