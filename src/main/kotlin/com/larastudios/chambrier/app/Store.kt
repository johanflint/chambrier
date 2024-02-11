package com.larastudios.chambrier.app

import com.larastudios.chambrier.app.domain.Event
import com.larastudios.chambrier.app.domain.Reducer
import com.larastudios.chambrier.app.domain.State
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
class Store(private val reducers: List<Reducer>) {
    private val flow = MutableStateFlow(State(mapOf()))

    suspend fun subscribe(events: Flow<Event>): Unit = coroutineScope {
        launch {
            events.collect { event ->
                val state = flow.value
                logger.debug { "Received event: $event" }
                flow.emit(reduce(event, state))
            }
        }
    }

    fun state(): SharedFlow<State> = flow.asSharedFlow()

    private fun reduce(event: Event, state: State): State =
        reducers.fold(state) { currentState, reducer ->
            reducer.reduce(event, currentState)
        }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
