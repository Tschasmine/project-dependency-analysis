package com.tschasmine.dependency.analysis.backend

import java.io.File
import org.slf4j.LoggerFactory

const val thirdPartyProject = "thirdparty"

private val logger = LoggerFactory.getLogger("com.tschasmine.dependency.analysis.backend.DependencyModel")

object ClassModelBuilder {

    fun buildFor(files: List<File>) = files.flatMap { file ->
        val analyzer = AstAnalyzer(file)
        analyzer.getClasses().map {
            ClassModel(it, file.getProject(), analyzer.getImports())
        }
    }

}

object DependencyModelBuilder {

    fun buildFor(files: List<File>): List<DependencyCollection> {
        val classModels = ClassModelBuilder.buildFor(files)
        return classModels.map { classModel ->
            val deps = classModel.imports.map { import ->
                Dependency(classModel,
                        when (import.type) {
                            ImportType.NORMAL -> classModels.find { it.name == import.name }?.project
                            ImportType.STATIC -> classModels.find { import.name.contains(it.name) }?.project
                            ImportType.ASTERISK -> classModels.find { it.name.contains(import.name) }?.project
                            ImportType.STATIC_ASTERISK -> classModels.find { it.name == import.name }?.project
                        } ?: thirdPartyProject,
                        import)
            }
            DependencyCollection(classModel, deps.groupBy({ it.project }, { it.import })
                    .entries.map { Project(it.key, it.value) })
        }
    }

}


fun File.getProject(): String {
    if (this.parent == "/") {
        throw CouldNotDetermineProjectException()
                .also { logger.error(it.message, it) }
    }
    if (this.parent != "src") {
        logger.debug("Source folder not reached, currently: '${this.absolutePath}'")
        this.parentFile.getProject()
    }
    return this.parentFile.name
}

data class ClassModel(val name: String, val project: String, val imports: List<Import>)

data class Project(val name: String, val usedClasses: List<Import>)

data class DependencyCollection(val originClassModel: ClassModel, val dependencies: List<Project>)

data class Dependency(val originClassModel: ClassModel, val project: String, val import: Import)

class CouldNotDetermineProjectException : Exception("Reached '/' without finding 'src' in the path.")
