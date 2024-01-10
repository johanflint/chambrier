package com.larastudios.chambrier.app.flowEngine

import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource

@SpringBootTest
class RiverEngineTest {
    val emptyFlow = ClassPathResource("flows/emptyFlow.json").getContentAsString(Charsets.UTF_8)
    val logFlow = ClassPathResource("flows/logFlow.json").getContentAsString(Charsets.UTF_8)

    @Autowired
    lateinit var factory: FlowFactory

    @Autowired
    lateinit var engine: FlowEngine

    @Test
    fun `executes an empty flow`() {
        val flow = factory.fromJson(emptyFlow)

        assertThatNoException()
            .isThrownBy {
                engine.execute(flow)
            }
    }

    @Test
    fun `executes a flow with one action node`() {
        val flow = factory.fromJson(logFlow)

        assertThatNoException()
            .isThrownBy {
                engine.execute(flow)
            }
    }

    @Test
    fun `executes an action for an action node`() {
        val action = mockk<Action>()
        justRun { action.execute() }

        val endNode = EndFlowNode("endNode")
        val logNode = ActionFlowNode("logNode", listOf(FlowLink(endNode)), action)
        val startNode = StartFlowNode("startNode", listOf(FlowLink(logNode)))
        val flow = Flow("flow", listOf(), startNode)

        engine.execute(flow)

        verify { action.execute() }
    }
}