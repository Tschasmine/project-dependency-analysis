package com.tschasmine.dependency.analysis.backend

import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import java.io.File
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("com.tschasmine.dependency.analysis.backend.DependencyModel")

object ClassModelBuilder {

    fun buildGraph(rootProject: File, files: List<File>): MutableGraph<ClassWithProject> {
        val graph = GraphBuilder.directed().build<ClassWithProject>()
        files.forEach { file ->
            AstAnalyzer(file).getClasses().forEach { graph.addNode(ClassWithProject(it, file.getProject(rootProject))) }
        }
        files.forEach { file ->
            val analyzer = AstAnalyzer(file)
            analyzer.getImports().forEach { import ->
                val originClass = graph.nodes().find { it.clazz == import.name || import.name.contains(it.clazz) }
                if (originClass != null) {
                    analyzer.getClasses().forEach { clazz ->
                        val importClass = graph.nodes().find { it.clazz == clazz }
                        if (importClass != null) {
                            graph.putEdge(originClass, importClass)
                        }
                    }
                }
            }
        }
        graph.edges().filter { it.source() == it.target() }.forEach { graph.removeEdge(it.source(), it.target()) }
        graph.nodes().filter { graph.outDegree(it) == 0 && graph.inDegree(it) == 0 }.forEach { graph.removeNode(it) }
        return graph
    }

    fun buildGraphFor(rootProject: File, files: List<File>, clazz: String): MutableGraph<ClassWithProject> {
        val graph = GraphBuilder.directed().build<ClassWithProject>()
        val analyzers = files
                .map { file ->
                    val analyzer = AstAnalyzer(file)
                    analyzer.getClasses().forEach { graph.addNode(ClassWithProject(it, file.getProject(rootProject))) }
                    analyzer
                }
                .flatMap { analyzer ->
                    analyzer.getClasses().map { clazz ->
                        clazz to analyzer
                    }
                }.toMap()
        addEdges(analyzers, clazz, graph)
        graph.nodes().filter { graph.outDegree(it) == 0 && graph.inDegree(it) == 0 }.forEach { graph.removeNode(it) }
        return graph
    }

    private fun addEdges(analyzers: Map<String, AstAnalyzer>, clazz: String, graph: MutableGraph<ClassWithProject>) {
        val analyzer = analyzers[clazz]
        analyzer?.getImports()?.forEach { import ->
            val importClass = graph.nodes().find { it.clazz == import.name || import.name.contains(it.clazz) }
            if (importClass != null) {
                analyzer.getClasses().forEach { clazz ->
                    val originClass = graph.nodes().find { it.clazz == clazz }
                    if (originClass != null) {
                        graph.putEdge(originClass, importClass)
                        addEdges(analyzers, importClass.clazz, graph)
                    }
                }
            }
        }
    }
}

fun File.getProject(rootProject: File) = this.absolutePath.replace("${rootProject.absolutePath}/", "")
        .also { logger.trace("Cut path: '$it'") }
        .split("/").firstOrNull()?.also { logger.debug("Found project: '$it'") }
        ?: throw CouldNotDetermineProjectException()

data class ClassWithProject(val clazz: String, val project: String)

class CouldNotDetermineProjectException : Exception("Reached '/' without finding 'src' in the path.")
