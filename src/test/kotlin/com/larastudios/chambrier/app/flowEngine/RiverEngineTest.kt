package com.larastudios.chambrier.app.flowEngine

import com.larastudios.chambrier.app.domain.*
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
        justRun { action.execute(any()) }

        val endNode = EndFlowNode("endNode")
        val logNode = ActionFlowNode("logNode", listOf(FlowLink(endNode)), action)
        val startNode = StartFlowNode("startNode", listOf(FlowLink(logNode)))
        val flow = Flow("flow", listOf(), startNode)

        engine.execute(flow)

        verify { action.execute(Scope()) }
    }


    @Test
    fun `executes a control device action`() {
        val endNode = EndFlowNode("endNode")
        val propertyMap = mapOf(
            "fan" to SetBooleanValue(true),
            "on" to ToggleBooleanValue,
            "brightness" to SetNumberValue(50),
            "fanSpeed" to IncrementNumberValue(10),
            "turnSpeed" to DecrementNumberValue(8),
            "color" to SetColorValue(CartesianCoordinate(0.1, 0.2), null),
            "colorWithGamut" to SetColorValue(
                CartesianCoordinate(0.3, 0.4),
                Gamut(
                    red = CartesianCoordinate(0.5, 0.6),
                    green = CartesianCoordinate(0.7, 0.8),
                    blue = CartesianCoordinate(0.9, 1.0),
                ),
            ),
            "button" to SetEnumValue(HueButtonState.ShortRelease)
        )
        val actionSpy = spyk(ControlDeviceAction("42", propertyMap))
        val actionNode = ActionFlowNode("controlNode", listOf(FlowLink(endNode)), actionSpy)
        val startNode = StartFlowNode("startNode", listOf(FlowLink(actionNode)))
        val flow = Flow("flow", listOf(), startNode)

        val report = engine.execute(flow)

        verify { actionSpy.execute(any<Scope>()) }
        assertThat(report.scope).containsKey(ControlDeviceAction.COMMAND_MAP)
        assertThat(report.scope[ControlDeviceAction.COMMAND_MAP]).isEqualTo(
            mutableMapOf("42" to propertyMap)
        )
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

        verify { logActionTrueSpy.execute(any()) }
        verify(exactly = 0) { logActionFalseSpy.execute(Scope()) }
        confirmVerified(logActionFalseSpy)
    }
}