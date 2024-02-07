package com.larastudios.chambrier.app

import com.larastudios.chambrier.app.domain.Event
import com.larastudios.chambrier.app.domain.Reducer
import com.larastudios.chambrier.app.domain.State
import com.larastudios.chambrier.lightDevice
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Store")
class StoreTest {
    @Test
    fun `calls reduce on all reducers`() = runTest {
        val reducer = mockk<Reducer>()
        val reducer2 = mockk<Reducer>()

        val store = Store(listOf(reducer, reducer2))

        val initialState = State(mapOf())
        val event = mockk<Event>()

        every { reducer.reduce(event, any()) } returns initialState
        every { reducer2.reduce(event, any()) } returns initialState

        val events = flowOf(event)
        store.subscribe(events)

        verify { reducer.reduce(event, any()) }
        verify { reducer2.reduce(event, any()) }
    }

    @Test
    fun `publishes the updated state`() = runTest {
        val reducer = mockk<Reducer>()
        val store = Store(listOf(reducer))

        val event = mockk<Event>()

        val updatedState = State(mapOf(lightDevice.id to lightDevice))
        every { reducer.reduce(event, any()) } returns updatedState

        assertThat(store.state().replayCache.first()).isEqualTo(State(mapOf()))
        store.subscribe(flowOf(event))
        assertThat(store.state().replayCache.first()).isEqualTo(updatedState)
    }
}
