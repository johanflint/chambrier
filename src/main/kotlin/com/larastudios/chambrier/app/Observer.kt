package com.larastudios.chambrier.app

import com.larastudios.chambrier.app.domain.Event
import reactor.core.publisher.Flux

interface Observer {
    fun observe(): Flux<Event>
}
