package com.larastudios.chambrier.app.flowEngine

import com.larastudios.chambrier.app.domain.*
import com.larastudios.chambrier.app.flowEngine.expression.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource

@SpringBootTest
@DisplayName("FlowFactory")
class FlowFactoryTest {
    val unknownNodeTypeFlow = ClassPathResource("flows/invalid/unknownNodeTypeFlow.json").getContentAsString(Charsets.UTF_8)
    val multipleStartNodesFlow = ClassPathResource("flows/invalid/multipleStartNodesFlow.json").getContentAsString(Charsets.UTF_8)
    val missingEndNodeFlow = ClassPathResource("flows/invalid/missingEndNodeFlow.json").getContentAsString(Charsets.UTF_8)
    val unconnectedNodeFlow = ClassPathResource("flows/invalid/unconnectedNodeFlow.json").getContentAsString(Charsets.UTF_8)
    val unusedNodesFlow = ClassPathResource("flows/invalid/unusedNodesFlow.json").getContentAsString(Charsets.UTF_8)
    val missingNodeFromConditionalFlow = ClassPathResource("flows/invalid/missingNodeFromConditionalFlow.json").getContentAsString(Charsets.UTF_8)

    val emptyFlow = ClassPathResource("flows/emptyFlow.json").getContentAsString(Charsets.UTF_8)
    val logFlow = ClassPathResource("flows/logFlow.json").getContentAsString(Charsets.UTF_8)
    val conditionalFlow = ClassPathResource("flows/conditionalFlow.json").getContentAsString(Charsets.UTF_8)
    val nestedConditionalFlow = ClassPathResource("flows/nestedConditionalFlow.json").getContentAsString(Charsets.UTF_8)
    val controlDeviceFlow = ClassPathResource("flows/controlDeviceFlow.json").getContentAsString(Charsets.UTF_8)

    @Autowired
    lateinit var factory: FlowFactory

    @Test
    fun `throws an exception if an unknown node type is found`() {
        assertThatExceptionOfType(UnknownNodeTypeException::class.java)
            .isThrownBy {
                factory.fromJson(unknownNodeTypeFlow)
            }
            .withMessage("unknownNode")
    }

    @Test
    fun `throws an exception if no start node is found`() {
        val flow = """{ "name": "flow", "nodes": [] }"""

        assertThatExceptionOfType(MissingStartNodeException::class.java)
            .isThrownBy {
                factory.fromJson(flow)
            }
    }

    @Test
    fun `throws an exception if multiple start nodes are found`() {
        assertThatExceptionOfType(TooManyStartNodesException::class.java)
            .isThrownBy {
                factory.fromJson(multipleStartNodesFlow)
            }
            .withMessage("Only one start node is allowed, found 2")
    }

    @Test
    fun `throws an exception if no end node is found`() {
        assertThatExceptionOfType(MissingEndNodeException::class.java)
            .isThrownBy {
                factory.fromJson(missingEndNodeFlow)
            }
    }

    @Test
    fun `throws an exception if a node is not connected`() {
        assertThatExceptionOfType(NoConnectingNodeException::class.java)
            .isThrownBy {
                factory.fromJson(unconnectedNodeFlow)
            }
            .withMessage("No links found to node 'endNode' in flow 'emptyFlow'")
    }

    @Test
    fun `throws an exception if a conditional node points to a non-existing node`() {
        assertThatExceptionOfType(MissingNodeException::class.java)
            .isThrownBy {
                factory.fromJson(missingNodeFromConditionalFlow)
            }
            .withMessage("Node 'conditionalNode' has a missing outgoing node to 'missingNode'")
    }

    @Test
    fun `throws an exception if not all nodes are connected`() {
        assertThatExceptionOfType(UnusedNodesException::class.java)
            .isThrownBy {
                factory.fromJson(unusedNodesFlow)
            }
            .withMessage("Unused nodes: unusedEndNode")
    }

    @Test
    fun `creates a flow consisting of a start and end node`() {
        val flow = factory.fromJson(emptyFlow)

        val endNode = EndFlowNode("endNode")
        val startNode = StartFlowNode("startNode", listOf(FlowLink(endNode)))

        assertThat(flow.name).isEqualTo("emptyFlow")
        assertThat(flow.nodes)
            .contains(startNode)
            .contains(endNode)
            .hasSize(2)
        assertThat(flow.startNode).isEqualTo(startNode)
    }

    @Test
    fun `creates a flow with an action node of type log`() {
        val flow = factory.fromJson(logFlow)

        val endNode = EndFlowNode("endNode")
        val logNode = ActionFlowNode("logNode", listOf(FlowLink(endNode)), LogAction("Action is triggered"))
        val startNode = StartFlowNode("startNode", listOf(FlowLink(logNode)))

        assertThat(flow.name).isEqualTo("logFlow")
        assertThat(flow.nodes)
            .contains(startNode)
            .contains(logNode)
            .contains(endNode)
            .hasSize(3)
        assertThat(flow.startNode).isEqualTo(startNode)
    }

    @Test
    fun `creates a flow with an action node of type control device`() {
        val flow = factory.fromJson(controlDeviceFlow)

        val endNode = EndFlowNode("endNode")
        val propertyMap = mapOf(
            "fan" to SetBooleanValue(true),
            "on" to ToggleBooleanValue,
            "brightness" to SetNumberValue(50),
            "fanSpeed" to IncrementNumberValue(10),
            "turnSpeed" to DecrementNumberValue(8),
            "color" to SetColorValue(CartesianCoordinate(0.1, 0.2)),
            "button" to SetEnumValue(HueButtonState.ShortRelease)
        )
        val actionNode = ActionFlowNode("controlNode", listOf(FlowLink(endNode)), ControlDeviceAction("42", propertyMap))
        val startNode = StartFlowNode("startNode", listOf(FlowLink(actionNode)))

        assertThat(flow.name).isEqualTo("controlDeviceFlow")
        assertThat(flow.nodes)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(listOf(startNode, actionNode, endNode))
        assertThat(flow.startNode).isEqualTo(startNode)
    }

    @Test
    fun `creates a flow with a conditional node`() {
        val flow = factory.fromJson(conditionalFlow)

        val endNode = EndFlowNode("endNode")

        val expression = EqualToExpression(ConstantValueExpression(42), ConstantValueExpression(42))
        val conditionalNode = ConditionalFlowNode("conditionalNode", listOf(FlowLink(endNode, true), FlowLink(endNode, false)), expression)
        val startNode = StartFlowNode("startNode", listOf(FlowLink(conditionalNode)))

        assertThat(flow.name).isEqualTo("conditionalFlow")
        assertThat(flow.nodes)
            .contains(startNode)
            .contains(conditionalNode)
            .contains(endNode)
            .hasSize(3)
        assertThat(flow.startNode).isEqualTo(startNode)
    }

    @Test
    fun `creates a flow with a conditional node with nested expressions`() {
        val flow = factory.fromJson(nestedConditionalFlow)

        val endNode = EndFlowNode("endNode")

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

        val conditionalNode = ConditionalFlowNode("conditionalNode", listOf(FlowLink(endNode, true), FlowLink(endNode, false)), expression)
        val startNode = StartFlowNode("startNode", listOf(FlowLink(conditionalNode)))

        assertThat(flow.name).isEqualTo("nestedConditionalFlow")
        assertThat(flow.nodes)
            .contains(startNode)
            .contains(conditionalNode)
            .contains(endNode)
            .hasSize(3)
        assertThat(flow.startNode).isEqualTo(startNode)
    }
}
