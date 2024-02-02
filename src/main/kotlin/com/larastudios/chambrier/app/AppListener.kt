package com.larastudios.chambrier.app

import com.larastudios.chambrier.app.domain.ControlDeviceCommand
import com.larastudios.chambrier.app.domain.FlowContext
import com.larastudios.chambrier.app.flowEngine.ControlDeviceAction
import com.larastudios.chambrier.app.flowEngine.FlowEngine
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.reactive.asFlow
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

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
            store.state().asFlow()
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

        val events = Flux.merge(
            observers.map {
                logger.info { "Starting ${it::class.simpleName}" }
                it.observe().publishOn(Schedulers.parallel())
            })

        store.subscribe(events)
        logger.info { "Store subscribed to event stream" }

        store.state().log().subscribe()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

@Suppress("UNCHECKED_CAST")
private fun getCommandMap(data: Map<String, Any>): CommandMap = data[ControlDeviceAction.COMMAND_MAP] as? CommandMap ?: mapOf()
