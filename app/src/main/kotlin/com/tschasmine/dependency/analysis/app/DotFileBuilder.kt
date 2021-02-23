package com.tschasmine.dependency.analysis.app

import com.tschasmine.dependency.analysis.backend.DependencyCollection
import com.tschasmine.dependency.analysis.backend.DependencyModelBuilder
import com.tschasmine.dependency.analysis.backend.DependencyModelBuilder.thirdPartyProject
import com.tschasmine.dependency.analysis.backend.FileCollector
import com.tschasmine.dependency.analysis.backend.Import
import com.tschasmine.dependency.analysis.backend.ImportType
import io.github.livingdocumentation.dotdiagram.DotGraph
import java.io.File
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(DotFileBuilder::class.java)

@Suppress("ComplexCondition")
class DotFileBuilder(private val outputDir: String = "") {

    fun detailedBuild(dir: File, project: String = ""): File {
        val dependencies = DependencyModelBuilder.buildFor(dir, FileCollector(dir).collectFiles("java", false))
        val projects = dependencies
                .flatMap { dep ->
                    dep.dependencies.map { project ->
                        Pair(project.name, project.usedClasses.map { import ->
                            if (import.type == ImportType.STATIC) {
                                Import(import.name.dropLastWhile { it != '.' }.dropLast(1), ImportType.NORMAL)
                            } else {
                                import
                            }
                        })
                    }
                }
                .plus(dependencies.map { dep ->
                    Pair(dep.originClassModel.project, listOf(Import(dep.originClassModel.name, ImportType.NORMAL)))
                })
                .filter{ it.first != thirdPartyProject }
                .groupBy({ it.first }, { it.second })
                .map { ProjectWithImports(it.key, it.value.flatten().distinct()) }
        logger.info("Found ${projects.size} projects: \n" +
                projects.map { it.name }.joinToString("\n") { "* $it" })

        val coloredProjects = ColoredProjects(projects.map { it.name }.toSet())

        val graph = DotGraph("Dependency Analysis")
        val diGraph = graph.digraph
        addClustersAndNodes(projects, diGraph, coloredProjects)
        addAssociations(dependencies, diGraph, coloredProjects)

        return buildFile(project, dir, graph)
    }

    fun build(dir: File, project: String = "", withThirdParty: Boolean = false): File {
        val dependencies = DependencyModelBuilder.buildFor(dir, FileCollector(dir).collectFiles("java", false))
        val projects = dependencies.flatMap { dep -> dep.dependencies.map { it.name } }.toSet()
        logger.info("Found ${projects.size} projects: \n${projects.joinToString("\n") { "* $it" }}")

        val coloredProjects = ColoredProjects(projects)

        val graph = DotGraph("Dependency Analysis")
        val diGraph = graph.digraph
        addNodes(projects, diGraph, coloredProjects)
        addAssociations(dependencies, withThirdParty, project, diGraph, coloredProjects)

        return buildFile(project, dir, graph)
    }

    private fun buildFile(project: String, dir: File, graph: DotGraph) =
        File("${if (outputDir.isBlank()) "./build" else outputDir}/${if (project.isEmpty()) dir.name else project}.dot")
                .also { it.writeText(graph.render().trim()) }
                .also { logger.info("Created dot file '${it.absolutePath}'") }

    private fun addClustersAndNodes(projects: List<ProjectWithImports>, diGraph: DotGraph.Digraph, coloredProjects: ColoredProjects) {
        projects.forEach { projectWithImports ->
            diGraph.addCluster(projectWithImports.name).apply {
                label = projectWithImports.name
                options = "color=${coloredProjects.getColorFor(projectWithImports.name)}"
                projectWithImports.imports.forEach { import ->
                    addNode(import.name).apply {
                        label = import.name
                        comment = import.type.name
                        options = "color=${coloredProjects.getColorFor(projectWithImports.name)}"
                    }
                }
            }
        }
    }

    private fun addNodes(projects: Set<String>, diGraph: DotGraph.Digraph, coloredProjects: ColoredProjects) {
        projects.filter { it != thirdPartyProject }.forEach { projectAsNode ->
            diGraph.addNode(projectAsNode).apply {
                label = projectAsNode
                options = "color=${coloredProjects.getColorFor(projectAsNode)}"
            }
        }
    }

    private fun addAssociations(dependencies: List<DependencyCollection>, diGraph: DotGraph.Digraph, coloredProjects: ColoredProjects) {
        dependencies.forEach { depCollection ->
            val currentClass = depCollection.originClassModel.name
            depCollection.dependencies.forEach { project ->
                project.usedClasses.forEach { import ->
                    val importName = if (import.type == ImportType.STATIC)
                        import.name.dropLastWhile { it != '.' }.dropLast(1)
                    else
                        import.name
                    val projectOfCurrentImport = dependencies.flatMap { it.dependencies }.find { it.usedClasses.contains(import) }
                    if (projectOfCurrentImport == null || projectOfCurrentImport.name != thirdPartyProject) {
                        logger.debug("Adding association $currentClass -> $importName")
                        diGraph.addAssociation(currentClass, importName).apply {
                            options = "color=${coloredProjects.getColorFor(project.name)}"
                        }
                    } else {
                        logger.debug("Skipping association of $currentClass -> $importName")
                    }
                }
            }
        }
    }

    private fun addAssociations(dependencies: List<DependencyCollection>, withThirdParty: Boolean, project: String, diGraph: DotGraph.Digraph, coloredProjects: ColoredProjects) {
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
    }

    fun render(dotFile: File): File {
        val imageFileName = "${dotFile.parentFile}/${dotFile.nameWithoutExtension}.png"
        val start = System.currentTimeMillis()
        "/usr/local/bin/dot -T png ${dotFile.absolutePath} -Gdpi=500 -o $imageFileName".runCommand()
        val end = System.currentTimeMillis()
        val imageFile = File(imageFileName)
        if (!imageFile.exists()) {
            throw DotFileCouldNotBeParsedToPngException(dotFile).also {
                logger.error(it.message, it)
            }
        }
        logger.info("Create image file '${imageFile.absolutePath}'")
        logger.debug("Image file creation took ${(end - start) / 1000}s")
        return imageFile
    }

    @Suppress("SpreadOperator")
    private fun String.runCommand(workingDir: File = File(".")) {
        ProcessBuilder(*split(" ").toTypedArray())
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
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

    private data class ProjectWithImports(val name: String, val imports: List<Import>)

}
