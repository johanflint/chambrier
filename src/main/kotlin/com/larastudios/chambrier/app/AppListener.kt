package com.larastudios.chambrier.app

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

@Component
class AppListener(val observers: List<Observer>, val store: Store) : ApplicationListener<ContextRefreshedEvent> {
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
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
