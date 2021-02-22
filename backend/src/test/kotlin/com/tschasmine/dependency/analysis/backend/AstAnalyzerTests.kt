package com.tschasmine.dependency.analysis.backend

import java.io.File
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsIterableContainingInOrder.contains
import org.junit.jupiter.api.Test

class AstAnalyzerTests {

    private val rootDir = "src/test/resources/test"

    @Test
    fun `Can collect all classes`() {
        val testFile = File("$rootDir/app/src/main/java/com/example/app/App.java")

        assertThat(AstAnalyzer(testFile).getClasses(), contains("com.example.app.App"))
    }

    @Test
    fun `Can collect imports`() {
        val testFile = File("$rootDir/app/src/main/java/com/example/app/App.java")
        val astAnalyzer = AstAnalyzer(testFile)

        assertThat(astAnalyzer.getImports(), contains("com.example.list.LinkedList",
                "org.apache.commons.text.WordUtils"))
        assertThat(astAnalyzer.getStaticImports(), contains("com.example.utilities.StringUtils.join",
                "com.example.utilities.StringUtils.split",
                "com.example.app.MessageUtils.getMessage"))
        assertThat(astAnalyzer.getAsteriskImports(), contains("java.util"))
    }

    @Test
    fun `Can collect static asterisk imports`() {
        val testFile = File("$rootDir/list/src/test/java/com/example/list/LinkedListTest.java")

        assertThat(AstAnalyzer(testFile).getStaticAsteriskImports(), contains("org.junit.jupiter.api.Assertions"))
    }

}
