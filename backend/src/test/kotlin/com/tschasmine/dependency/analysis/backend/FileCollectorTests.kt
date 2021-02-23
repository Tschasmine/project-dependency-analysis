package com.tschasmine.dependency.analysis.backend

import java.io.File
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Test

class FileCollectorTests {

    @Test
    fun `Can collect Java files from root project dir`() {
        val javaFiles = FileCollector(File(rootDir))
                .collectFiles("java")

        assertThat(javaFiles.map { it.name }, containsInAnyOrder("App.java",
                "MessageUtils.java",
                "MessageUtilsTest.java",
                "LinkedList.java",
                "LinkedListTest.java",
                "JoinUtils.java",
                "SplitUtils.java",
                "StringUtils.java"))
    }

    @Test
    fun `Can collect Java files without tests from root project dir`() {
        val javaFiles = FileCollector(File(rootDir))
                .collectFiles("java", false)

        assertThat(javaFiles.map { it.name }, containsInAnyOrder("App.java",
                "MessageUtils.java",
                "LinkedList.java",
                "JoinUtils.java",
                "SplitUtils.java",
                "StringUtils.java"))
    }

}
