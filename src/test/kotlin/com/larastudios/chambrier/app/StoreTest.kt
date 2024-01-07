package com.larastudios.chambrier.app

import com.larastudios.chambrier.app.domain.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

@DisplayName("Store")
class StoreTest {
    @Test
    fun `calls reduce on all reducers`() {
        val reducer = mockk<Reducer>()
        val reducer2 = mockk<Reducer>()

        val store = Store(listOf(reducer, reducer2))

        val initialState = State(mapOf())
        val event = mockk<Event>()

        every { reducer.reduce(event, any()) } returns initialState
        every { reducer2.reduce(event, any()) } returns initialState

        val events = Flux.just(event)
        store.subscribe(events)

        verify { reducer.reduce(event, any()) }
        verify { reducer2.reduce(event, any()) }
    }

    @Test
    fun `publishes the updated state`() {
        val reducer = mockk<Reducer>()
        val store = Store(listOf(reducer))

        val event = mockk<Event>()

        val updatedState = State(mapOf(device.id to device))
        every { reducer.reduce(event, any()) } returns updatedState

        StepVerifier.create(store.state())
            .then {
                store.subscribe(Flux.just(event))
            }
            .expectNext(State(mapOf()))
            .expectNext(updatedState)
            .thenCancel()
            .verify()
    }
}

private val device = Device(
    "90bdce60-3704-470e-be4c-8264f2bc8151",
    DeviceType.Light,
    "Signify Netherlands B.V.",
    "LWA021",
    "Hue filament bulb",
     "Livingroom",
    mapOf())
