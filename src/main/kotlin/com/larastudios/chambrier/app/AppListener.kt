package com.larastudios.chambrier.app

import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

@Component
class AppListener(val observers: List<Observer>) : ApplicationListener<ContextRefreshedEvent> {
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        for (observer in observers) {
            observer.observe()
                .log()
                .subscribe()
        }
    }
}
