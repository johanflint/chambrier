package com.larastudios.chambrier.app

import com.larastudios.chambrier.app.domain.Event
import kotlinx.coroutines.flow.Flow

interface Observer {
    suspend fun observe(): Flow<Event>
}
