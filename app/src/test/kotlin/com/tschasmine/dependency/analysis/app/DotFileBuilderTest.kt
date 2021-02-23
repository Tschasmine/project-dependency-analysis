package com.tschasmine.dependency.analysis.app

import java.io.File
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.io.FileMatchers.anExistingFile
import org.junit.jupiter.api.Test

class DotFileBuilderTest {

    private val testProject = File("../backend/src/test/resources/test")

    @Test
    fun `Can create image for all projects`() {
        val dotFileBuilder = DotFileBuilder()
        val dotFile = dotFileBuilder.build(testProject, withThirdParty = true)
        assertThat(dotFileBuilder.render(dotFile), anExistingFile())
    }

    @Test
    fun `Can create files for specific projects`() {
        val dotFileBuilder = DotFileBuilder()
        val dotFile = dotFileBuilder.build(testProject, "list")
        assertThat(dotFileBuilder.render(dotFile), anExistingFile())
    }

    @Test
    fun `Can create detailed view`() {
        val dotFileBuilder = DotFileBuilder()
        val dotFile = dotFileBuilder.detailedBuild(testProject)
        assertThat(dotFileBuilder.render(dotFile), anExistingFile())
    }

}
