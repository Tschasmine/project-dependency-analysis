package com.tschasmine.dependency.analysis.app

import java.io.File
import org.junit.jupiter.api.Test

class DotFileBuilderTest {

    @Test
    fun test() {
        val dotFileBuilder = DotFileBuilder()
        val dotFile = dotFileBuilder.build(File("/Users/jazz/Workspaces/work/eport"))
        dotFileBuilder.write(dotFile)
    }

    @Test
    fun `Can create files for specific projects`() {
        val dotFileBuilder = DotFileBuilder()
        val dotFile = dotFileBuilder.build(File("/Users/jazz/Workspaces/work/eport"), "questionnaire-view")
        dotFileBuilder.write(dotFile)
    }

}
