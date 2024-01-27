package com.larastudios.chambrier.app

import com.larastudios.chambrier.app.domain.ControlDeviceCommand
import com.larastudios.chambrier.app.flowEngine.ControlDeviceAction
import com.larastudios.chambrier.app.flowEngine.FlowEngine
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
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
) : ApplicationListener<ContextRefreshedEvent> {
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val flows = flowLoader.load()

        store.state()
            .doOnNext {
                val commands = flows.map(flowEngine::execute)
                    .map {
                        getCommandMap(it.scope)
                    }
                    .fold(mapOf(), ::mergeCommandMaps)
                    .map { (deviceId, propertyMap) -> ControlDeviceCommand(deviceId, propertyMap) }

                controllers.forEach {
                    it.send(commands)
                }
            }.subscribe()

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
