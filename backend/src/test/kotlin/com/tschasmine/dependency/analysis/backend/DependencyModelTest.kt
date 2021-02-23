package com.tschasmine.dependency.analysis.backend

import java.io.File
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Test

class DependencyModelTest {

    @Test
    fun `Can get containing sub-project`() {
        val testFile = File("$rootDir/app/src/main/java/com/example/app/App.java")

        assertThat(testFile.getProject(), `is`("app"))
    }

    @Test
    fun `Can build class models`() {
        val testFiles = listOf(
                File("$rootDir/app/src/main/java/com/example/app/App.java"),
                File("$rootDir/list/src/main/java/com/example/list/LinkedList.java"),
                File("$rootDir/utilities/src/main/java/com/example/utilities/JoinUtils.java")
        )

        val classModels = ClassModelBuilder.buildFor(testFiles)

        assertThat(classModels, containsInAnyOrder(
                ClassModel("com.example.app.App", "app", listOf(
                        Import("com.example.list.LinkedList", ImportType.NORMAL),
                        Import("com.example.utilities.StringUtils.join", ImportType.STATIC),
                        Import("com.example.utilities.StringUtils.split", ImportType.STATIC),
                        Import("com.example.app.MessageUtils.getMessage", ImportType.STATIC),
                        Import("org.apache.commons.text.WordUtils", ImportType.NORMAL),
                        Import("java.util", ImportType.ASTERISK)
                )),
                ClassModel("com.example.list.LinkedList", "list", listOf()),
                ClassModel("com.example.utilities.JoinUtils", "utilities", listOf(
                        Import("com.example.list.LinkedList", ImportType.NORMAL)
                ))
        ))
    }

    @Test
    fun `Can build dependency model`() {
        val testFiles = listOf(
                File("$rootDir/app/src/main/java/com/example/app/App.java"),
                File("$rootDir/list/src/main/java/com/example/list/LinkedList.java"),
                File("$rootDir/utilities/src/main/java/com/example/utilities/StringUtils.java")
        )

        val classApp = DependencyCollection(
                ClassModel("com.example.app.App", "app", listOf(
                        Import("com.example.list.LinkedList", ImportType.NORMAL),
                        Import("com.example.utilities.StringUtils.join", ImportType.STATIC),
                        Import("com.example.utilities.StringUtils.split", ImportType.STATIC),
                        Import("com.example.app.MessageUtils.getMessage", ImportType.STATIC),
                        Import("org.apache.commons.text.WordUtils", ImportType.NORMAL),
                        Import("java.util", ImportType.ASTERISK)
                )),
                listOf(
                        Project("list", listOf(Import("com.example.list.LinkedList", ImportType.NORMAL))),
                        Project("utilities", listOf(
                                Import("com.example.utilities.StringUtils.join", ImportType.STATIC),
                                Import("com.example.utilities.StringUtils.split", ImportType.STATIC)
                        )),
                        Project("thirdparty", listOf(
                                Import("com.example.app.MessageUtils.getMessage", ImportType.STATIC),
                                Import("org.apache.commons.text.WordUtils", ImportType.NORMAL),
                                Import("java.util", ImportType.ASTERISK)
                        ))
                )
        )
        val classLinkedList = DependencyCollection(
                ClassModel("com.example.list.LinkedList", "list", emptyList()),
                emptyList()
        )
        val classStringUtils = DependencyCollection(
                ClassModel("com.example.utilities.StringUtils", "utilities", listOf(
                        Import("com.example.list.LinkedList", ImportType.NORMAL))
                ),
                listOf(
                        Project("list", listOf(Import("com.example.list.LinkedList", ImportType.NORMAL)))
                )
        )
        val dependencies = DependencyModelBuilder.buildFor(testFiles)
        assertThat(dependencies, containsInAnyOrder(classApp, classLinkedList, classStringUtils))
    }

}
