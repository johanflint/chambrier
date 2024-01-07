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
        logger.info { "Found ${observers.size} observers..." }

        val events = Flux.merge(
            observers.map {
                it.observe().publishOn(Schedulers.parallel())
            })

        logger.info { "Store subscribing to event stream..." }
        store.subscribe(events)
        logger.info { "Store subscribing to event stream... OK" }

        store.state().log().subscribe()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
