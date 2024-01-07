package com.larastudios.chambrier.app.domain

interface Reducer {
    fun reduce(event: Event, state: State): State
}
