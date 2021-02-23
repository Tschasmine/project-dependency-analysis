package com.tschasmine.dependency.analysis.backend

import java.io.File
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsIterableContainingInOrder.contains
import org.junit.jupiter.api.Test

class AstAnalyzerTests {

    @Test
    fun `Can collect all classes`() {
        val testFile = File("$rootDir/app/src/main/java/com/example/app/App.java")

        assertThat(AstAnalyzer(testFile).getClasses(), contains("com.example.app.App"))
    }

    @Test
    fun `Can collect imports`() {
        val testFile = File("$rootDir/app/src/main/java/com/example/app/App.java")
        val astAnalyzer = AstAnalyzer(testFile)

        assertThat(astAnalyzer.getImports().filter { it.type == ImportType.NORMAL }.map { it.name },
                contains("com.example.list.LinkedList", "org.apache.commons.text.WordUtils"))
        assertThat(astAnalyzer.getImports().filter { it.type == ImportType.STATIC }.map { it.name },
                contains(
                        "com.example.utilities.StringUtils.join",
                        "com.example.utilities.StringUtils.split",
                        "com.example.app.MessageUtils.getMessage"
                ))
        assertThat(astAnalyzer.getImports().filter { it.type == ImportType.ASTERISK }.map { it.name },
                contains("java.util"))
    }

    @Test
    fun `Can collect static asterisk imports`() {
        val testFile = File("$rootDir/list/src/test/java/com/example/list/LinkedListTest.java")

        assertThat(AstAnalyzer(testFile).getImports(),
                contains(
                        Import("org.junit.jupiter.api.Test", ImportType.NORMAL),
                        Import("org.junit.jupiter.api.Assertions", ImportType.STATIC_ASTERISK)
                )
        )
    }

}
