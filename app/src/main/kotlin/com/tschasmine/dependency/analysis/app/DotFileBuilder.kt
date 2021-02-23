package com.tschasmine.dependency.analysis.app

import com.tschasmine.dependency.analysis.backend.DependencyModelBuilder
import com.tschasmine.dependency.analysis.backend.FileCollector
import com.tschasmine.dependency.analysis.backend.DependencyModelBuilder.thirdPartyProject
import io.github.livingdocumentation.dotdiagram.DotGraph
import java.io.File
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(DotFileBuilder::class.java)

@Suppress("ComplexCondition")
class DotFileBuilder {

    fun build(dir: File, project: String = "", withThirdParty: Boolean = false): File {
        val dependencies = DependencyModelBuilder.buildFor(dir, FileCollector(dir).collectFiles("java", false))
        val projects = dependencies.flatMap { dep -> dep.dependencies.map { it.name } }.toSet()
        logger.info("Found ${projects.size} projects: \n${projects.joinToString("\n") { "* $it" }}")

        val coloredProjects = ColoredProjects(projects)

        val graph = DotGraph("Dependency Analysis")
        val diGraph = graph.digraph
        projects.filter { it != thirdPartyProject }.forEach { projectAsNode ->
            diGraph.addNode(projectAsNode).apply {
                label = projectAsNode
                options = "color=${coloredProjects.getColorFor(projectAsNode)}"
            }
        }
        dependencies.forEach { dependencyCollection ->
            dependencyCollection.dependencies.forEach { dependentProject ->
                val currentProject = dependencyCollection.originClassModel.project
                if (currentProject != dependentProject.name
                        && (withThirdParty || dependentProject.name != thirdPartyProject)
                        && (project.isEmpty() || currentProject == project)) {
                    diGraph.addAssociation(currentProject, dependentProject.name).apply {
                        options = "color=${coloredProjects.getColorFor(dependentProject.name)}"
                    }
                }
            }
        }
        return File("build/${if (project.isEmpty()) dir.name else project}.dot")
                .also { it.writeText(graph.render().trim()) }
                .also { logger.info("Created dot file '${it.absolutePath}'") }
    }

    fun write(dotFile: File) {
        val imageFile = "${dotFile.parentFile}/${dotFile.nameWithoutExtension}.png"
        "/usr/local/bin/dot -T png ${dotFile.absolutePath} -Gdpi=500 -o $imageFile".runCommand()
        if (!File(imageFile).exists()) {
            throw DotFileCouldNotBeParsedToPngException(dotFile)
        }
        logger.info("Create image file '$imageFile'")
    }

    @Suppress("SpreadOperator")
    private fun String.runCommand(workingDir: File = File(".")) {
        ProcessBuilder(*split(" ").toTypedArray())
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
                .waitFor(1, TimeUnit.MINUTES)
    }

    class ColoredProjects(projects: Set<String>) {
        private val projects = projects.map {
            ColoredProject(it, Colors.values()[projects.indexOf(it)].name.toLowerCase())
        }

        private val defaultColor = "black"

        fun getColorFor(project: String) =
                projects.find { it.name == project }?.color ?: defaultColor

        data class ColoredProject(val name: String, val color: String)
    }

    class DotFileCouldNotBeParsedToPngException(dotFile: File) :
    Exception("Error while parsing dot file '${dotFile.absolutePath}' to image.")

}
