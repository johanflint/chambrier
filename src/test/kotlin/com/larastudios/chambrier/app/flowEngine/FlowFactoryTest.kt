package com.larastudios.chambrier.app.flowEngine

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource

@SpringBootTest
class FlowFactoryTest {
    val unknownNodeTypeFlow = ClassPathResource("flows/invalid/unknownNodeTypeFlow.json").getContentAsString(Charsets.UTF_8)
    val multipleStartNodesFlow = ClassPathResource("flows/invalid/multipleStartNodesFlow.json").getContentAsString(Charsets.UTF_8)
    val missingEndNodeFlow = ClassPathResource("flows/invalid/missingEndNodeFlow.json").getContentAsString(Charsets.UTF_8)
    val unconnectedNodeFlow = ClassPathResource("flows/invalid/unconnectedNodeFlow.json").getContentAsString(Charsets.UTF_8)
    val unusedNodesFlow = ClassPathResource("flows/invalid/unusedNodesFlow.json").getContentAsString(Charsets.UTF_8)

    val emptyFlow = ClassPathResource("flows/emptyFlow.json").getContentAsString(Charsets.UTF_8)

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

        assertThatExceptionOfType(MissingNodeException::class.java)
            .isThrownBy {
                factory.fromJson(flow)
            }
            .withMessage("No start node found")
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
        assertThatExceptionOfType(MissingNodeException::class.java)
            .isThrownBy {
                factory.fromJson(missingEndNodeFlow)
            }
            .withMessage("No end nodes found")
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
}
