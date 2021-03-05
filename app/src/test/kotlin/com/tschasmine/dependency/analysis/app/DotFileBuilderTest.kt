package com.tschasmine.dependency.analysis.app

import java.io.File
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.io.FileMatchers.anExistingFile
import org.junit.jupiter.api.Test

class DotFileBuilderTest {

    private val testProject = File("../backend/src/test/resources/test")

    @Test
    fun `Can create image for all projects`() {
        val dotFileBuilder = DotFileBuilder()
        val dotFile = dotFileBuilder.build(testProject)
        assertThat(dotFileBuilder.render(dotFile), anExistingFile())
        assertThat(dotFile.readText(), `is`("""# Class diagram Dependency Analysis
            |digraph G {
            |	graph [labelloc=top,label="Dependency Analysis",fontname="Verdana",fontsize=12,overlap=false];
            |	edge [fontname="Verdana",fontsize=9,labelfontname="Verdana",labelfontsize=9];
            |	node [fontname="Verdana",fontsize=9,shape=record];
            |	c0 [label="app", color=blue]
            |	c1 [label="utilities", color=darkcyan]
            |	c2 [label="list", color=forestgreen]
            |	// null
            |	c0 -> c0 [color=blue];
            |	// null
            |	c2 -> c1 [color=forestgreen];
            |	// null
            |	c2 -> c0 [color=forestgreen];
            |	// null
            |	c1 -> c0 [color=darkcyan];
            |}""".trimMargin()))
    }

    @Test
    fun `Can create detailed view`() {
        val dotFileBuilder = DotFileBuilder()
        val dotFile = dotFileBuilder.detailedBuild(testProject)
        assertThat(dotFileBuilder.render(dotFile), anExistingFile())
        assertThat(dotFile.readText(), `is`("""# Class diagram Dependency Analysis
            |digraph G {
            |	graph [labelloc=top,label="Dependency Analysis",fontname="Verdana",fontsize=12,overlap=false];
            |	edge [fontname="Verdana",fontsize=9,labelfontname="Verdana",labelfontsize=9];
            |	node [fontname="Verdana",fontsize=9,shape=record];
            |subgraph cluster_c0 {
            |label = "app";
            |	c1 [label="com.example.app.MessageUtils", color=blue]
            |	c2 [label="com.example.app.App", color=blue]
            |}
            |subgraph cluster_c3 {
            |label = "utilities";
            |	c4 [label="com.example.utilities.StringUtils", color=darkcyan]
            |	c5 [label="com.example.utilities.SplitUtils", color=darkcyan]
            |	c6 [label="com.example.utilities.JoinUtils", color=darkcyan]
            |}
            |subgraph cluster_c7 {
            |label = "list";
            |	c8 [label="com.example.list.LinkedList", color=forestgreen]
            |}
            |	// null
            |	c1 -> c2 [];
            |	// null
            |	c8 -> c4 [];
            |	// null
            |	c8 -> c6 [];
            |	// null
            |	c8 -> c5 [];
            |	// null
            |	c8 -> c2 [];
            |	// null
            |	c4 -> c2 [];
            |}""".trimMargin()))
    }

    @Test
    fun `Can create details for specific class`() {
        val dotFileBuilder = DotFileBuilder()
        val dotFile = dotFileBuilder.classView(testProject, "com.example.app.App")
        assertThat(dotFileBuilder.render(dotFile), anExistingFile())
        assertThat(dotFile.readText(), `is`("""# Class diagram Dependency Analysis
            |digraph G {
            |	graph [labelloc=top,label="Dependency Analysis",fontname="Verdana",fontsize=12,overlap=false];
            |	edge [fontname="Verdana",fontsize=9,labelfontname="Verdana",labelfontsize=9];
            |	node [fontname="Verdana",fontsize=9,shape=record];
            |subgraph cluster_c0 {
            |label = "app";
            |	c1 [label="com.example.app.MessageUtils", color=blue]
            |	c2 [label="com.example.app.App", color=blue]
            |}
            |subgraph cluster_c3 {
            |label = "utilities";
            |	c4 [label="com.example.utilities.StringUtils", color=darkcyan]
            |}
            |subgraph cluster_c5 {
            |label = "list";
            |	c6 [label="com.example.list.LinkedList", color=forestgreen]
            |}
            |	// null
            |	c2 -> c1 [];
            |	// null
            |	c4 -> c6 [];
            |	// null
            |	c2 -> c6 [];
            |	// null
            |	c2 -> c4 [];
            |}""".trimMargin()))
    }

}
