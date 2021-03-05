package com.tschasmine.dependency.analysis.backend

import java.io.File
import kotlin.io.path.ExperimentalPathApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Test

const val rootDir = "src/test/resources/test"

class IntegrationTest {

    @Test
    fun `Can build graph`() {
        val graph = ClassModelBuilder.buildGraph(File(rootDir), FileCollector(File(rootDir))
                .collectFiles("java"))
        assertThat(graph.nodes(), containsInAnyOrder(
                ClassWithProject("com.example.app.MessageUtils", "app"),
                ClassWithProject("com.example.app.App", "app"),
                ClassWithProject("com.example.utilities.StringUtils", "utilities"),
                ClassWithProject("com.example.utilities.SplitUtils", "utilities"),
                ClassWithProject("com.example.utilities.JoinUtils", "utilities"),
                ClassWithProject("com.example.list.LinkedList", "list")
        ))
        assertThat(graph.hasEdgeConnecting(
                ClassWithProject("com.example.app.MessageUtils", "app"),
                ClassWithProject("com.example.app.App", "app")),
                `is`(true))
        assertThat(graph.hasEdgeConnecting(
                ClassWithProject("com.example.utilities.StringUtils", "utilities"),
                ClassWithProject("com.example.app.App", "app")),
                `is`(true))
        assertThat(graph.hasEdgeConnecting(
                ClassWithProject("com.example.list.LinkedList", "list"),
                ClassWithProject("com.example.utilities.SplitUtils", "utilities")),
                `is`(true))
        assertThat(graph.hasEdgeConnecting(
                ClassWithProject("com.example.list.LinkedList", "list"),
                ClassWithProject("com.example.app.App", "app")),
                `is`(true))
        assertThat(graph.hasEdgeConnecting(
                ClassWithProject("com.example.list.LinkedList", "list"),
                ClassWithProject("com.example.utilities.StringUtils", "utilities")),
                `is`(true))
        assertThat(graph.hasEdgeConnecting(
                ClassWithProject("com.example.list.LinkedList", "list"),
                ClassWithProject("com.example.utilities.JoinUtils", "utilities")),
                `is`(true))
    }

    @Test
    fun `Can build graph for class`() {
        val graph = ClassModelBuilder.buildGraphFor(File(rootDir), FileCollector(File(rootDir))
                .collectFiles("java"), "com.example.utilities.JoinUtils")
        println(graph)
    }

    @Test
    fun `Can get containing sub-project`() {
        val testFile = File("$rootDir/app/src/main/java/com/example/app/App.java")

        assertThat(testFile.getProject(File(rootDir)), `is`("app"))
    }

}
