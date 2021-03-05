package com.tschasmine.dependency.analysis.app

import com.google.common.graph.MutableGraph
import com.tschasmine.dependency.analysis.backend.ClassModelBuilder
import com.tschasmine.dependency.analysis.backend.ClassWithProject
import com.tschasmine.dependency.analysis.backend.FileCollector
import io.github.livingdocumentation.dotdiagram.DotGraph
import java.io.File
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(DotFileBuilder::class.java)

@Suppress("ComplexCondition")
class DotFileBuilder(private val outputDir: String = "") {

    fun classView(dir: File, clazz: String, transitive: Boolean = false): File {
        val classGraph = ClassModelBuilder.buildGraphFor(dir, FileCollector(dir).collectFiles("java", false), clazz)
        val graph = visualize(classGraph)

        return buildFile(dir, graph, transitive, clazz)
    }

    fun detailedBuild(dir: File, transitive: Boolean = false): File {
        val classGraph = ClassModelBuilder.buildGraph(dir, FileCollector(dir).collectFiles("java", false))
        val graph = visualize(classGraph)

        return buildFile(dir, graph, transitive)
    }

    fun build(dir: File, transitive: Boolean = false): File {
        val graph = DotGraph("Dependency Analysis")
        val diGraph = graph.digraph

        val classGraph = ClassModelBuilder.buildGraph(dir, FileCollector(dir).collectFiles("java", false))
        val projects = classGraph.nodes().map { it.project }.toSet()
        val coloredProjects = ColoredProjects(projects)
        projects.forEach {
            diGraph.addNode(it).apply {
                label = it
                options = "color=${coloredProjects.getColorFor(it)}"
            }
        }

        classGraph.edges().forEach {
            if (it.target().project != it.source().project) {
                diGraph.addAssociation(it.target().project, it.source().project).apply {
                    options = "color=${coloredProjects.getColorFor(it.source().project)}"
                }
            }
        }

        return buildFile(dir, graph, transitive)
    }

    private fun visualize(classGraph: MutableGraph<ClassWithProject>): DotGraph {
        val graph = DotGraph("Dependency Analysis")
        val diGraph = graph.digraph

        val coloredProjects = ColoredProjects(classGraph.nodes().map { it.project }.toSet())
        classGraph.nodes().groupBy { it.project }.forEach { (project, classes) ->
            val cluster = diGraph.addCluster(project).apply {
                label = project
                options = "color=${coloredProjects.getColorFor(project)}"
            }
            classes.map { it.clazz }.forEach {
                cluster.addNode(it).apply {
                    label = it
                    options = "color=${coloredProjects.getColorFor(project)}"
                }
            }
        }
        classGraph.edges().forEach { edge ->
            diGraph.addAssociation(edge.source().clazz, edge.target().clazz)
        }
        return graph
    }

    private fun buildFile(dir: File, graph: DotGraph, transitive: Boolean, name: String = ""): File {
        val dotFile = File("${if (outputDir.isBlank()) "./build" else outputDir}/${if (name.isEmpty()) dir.name else name}.dot")
                .also {
                    it.writeText(graph.render().trim()
                            .replace("graph [labelloc=top,label=\"Dependency Analysis\",fontname=\"Verdana\",fontsize=12];",
                                    "graph [labelloc=top,label=\"Dependency Analysis\",fontname=\"Verdana\",fontsize=12,overlap=false];"))
                }
                .also { logger.info("Created dot file '${it.absolutePath}'") }
        return if (transitive) File(dotFile.parentFile, "${dotFile.nameWithoutExtension}-tred.${dotFile.extension}").also {
            "/usr/local/bin/tred ${dotFile.absolutePath}".runCommand(outputFile = it)
        } else dotFile
    }

    fun render(dotFile: File, large: Boolean = false): File {
        val imageFileName = "${dotFile.parentFile}/${dotFile.nameWithoutExtension}.svg"
        val start = System.currentTimeMillis()
        "/usr/local/bin/dot ${if (large) "-Ksfdp " else ""}-Tsvg ${dotFile.absolutePath} -o $imageFileName".runCommand()
        val end = System.currentTimeMillis()
        val imageFile = File(imageFileName)
        if (!imageFile.exists()) {
            throw DotFileCouldNotBeParsedToPngException(dotFile).also {
                logger.error(it.message, it)
            }
        }
        logger.info("Create image file '${imageFile.absolutePath}'")
        logger.info("Image file creation took ${(end - start) / 1000}s")
        return imageFile
    }

    @Suppress("SpreadOperator")
    private fun String.runCommand(workingDir: File = File("."), outputFile: File? = null) {
        ProcessBuilder(*split(" ").toTypedArray())
                .directory(workingDir)
                .redirectOutput(if (outputFile == null) ProcessBuilder.Redirect.INHERIT else ProcessBuilder.Redirect.to(outputFile))
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
                .waitFor(4, TimeUnit.HOURS)
    }

    class DotFileCouldNotBeParsedToPngException(dotFile: File) :
            Exception("Error while parsing dot file '${dotFile.absolutePath}' to image.")

    private class ColoredProjects(projects: Set<String>) {
        private val projects = projects.map {
            ColoredProject(it, Colors.values()[projects.indexOf(it)].name.toLowerCase())
        }

        private val defaultColor = "black"

        fun getColorFor(project: String) =
                projects.find { it.name == project }?.color ?: defaultColor

        data class ColoredProject(val name: String, val color: String)
    }

}
