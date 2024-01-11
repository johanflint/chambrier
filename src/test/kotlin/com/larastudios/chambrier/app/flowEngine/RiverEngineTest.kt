package com.larastudios.chambrier.app.flowEngine

import com.larastudios.chambrier.app.flowEngine.expression.*
import io.mockk.*
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

    @Test
    fun `executes a flow with a conditional node`() {
        val endNode = EndFlowNode("endNode")

        val logActionTrueSpy = spyk(LogAction("true"))
        val logActionFalseSpy = spyk(LogAction("false"))

        val logNodeTrue = ActionFlowNode("logNodeTrue", listOf(FlowLink(endNode)), logActionTrueSpy)
        val logNodeFalse = ActionFlowNode("logNodeFalse", listOf(FlowLink(endNode)), logActionFalseSpy)

        val expression = OrExpression(
            AndExpression(
                EqualToExpression(ConstantValueExpression(42), ConstantValueExpression(42)),
                GreaterThanExpression(ConstantValueExpression(4), ConstantValueExpression(8)),
            ),
            AndExpression(
                NotEqualToExpression(ConstantValueExpression(42), ConstantValueExpression(1337)),
                LessThanExpression(ConstantValueExpression(4), ConstantValueExpression(8)),
            )
        )
        val conditionalNode = ConditionalFlowNode("conditionalNode", listOf(FlowLink(logNodeTrue, true), FlowLink(logNodeFalse, false)), expression)
        val startNode = StartFlowNode("startNode", listOf(FlowLink(conditionalNode)))
        val flow = Flow("flow", listOf(), startNode)

        engine.execute(flow)

        verify { logActionTrueSpy.execute() }
        verify(exactly = 0) { logActionFalseSpy.execute() }
        confirmVerified(logActionFalseSpy)
    }
}