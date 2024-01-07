package com.larastudios.chambrier.app

import com.larastudios.chambrier.app.domain.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks

@Component
class Store(private val reducers: List<Reducer>) {
    private val sink = Sinks.many().replay().latestOrDefault(State(mapOf()))

    fun subscribe(events: Flux<Event>) {
        events.withLatestFrom(sink.asFlux()) { event, state ->
            logger.debug { "Received event: $event" }
            sink.tryEmitNext(reduce(event, state))
        }.subscribe()
    }

    fun state(): Flux<State> = sink.asFlux()

    private fun reduce(event: Event, state: State): State =
        reducers.fold(state) { currentState, reducer ->
            reducer.reduce(event, currentState)
        }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
