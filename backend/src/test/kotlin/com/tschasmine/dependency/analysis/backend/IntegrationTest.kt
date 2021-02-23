package com.tschasmine.dependency.analysis.backend

import java.io.File
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

const val rootDir = "src/test/resources/test"

class IntegrationTest {

    @Test
    fun `Can analyze test project dependencies`() {
        data class TestDependency(val name: String, val projects: List<String>)

        val dependencies = DependencyModelBuilder.buildFor(File(rootDir), FileCollector(File(rootDir)).collectFiles("java"))

        val testDependencies = dependencies.map { dep -> TestDependency(dep.originClassModel.name, dep.dependencies.map { it.name }) }

        println(testDependencies)

        assertThat(testDependencies.find { it.name == "com.example.app.MessageUtilsTest" }!!.projects, `is`(listOf("thirdparty")))
        assertThat(testDependencies.find { it.name == "com.example.app.MessageUtils" }!!.projects, `is`(emptyList()))
        assertThat(testDependencies.find { it.name == "com.example.app.App" }!!.projects, `is`(listOf("list", "utilities", "app", "thirdparty")))
        assertThat(testDependencies.find { it.name == "com.example.utilities.StringUtils" }!!.projects, `is`(listOf("list")))
        assertThat(testDependencies.find { it.name == "com.example.utilities.SplitUtils" }!!.projects, `is`(listOf("list")))
        assertThat(testDependencies.find { it.name == "com.example.utilities.JoinUtils" }!!.projects, `is`(listOf("list")))
        assertThat(testDependencies.find { it.name == "com.example.list.LinkedListTest" }!!.projects, `is`(listOf("thirdparty")))
        assertThat(testDependencies.find { it.name == "com.example.list.LinkedList" }!!.projects, `is`(emptyList()))
    }

}
