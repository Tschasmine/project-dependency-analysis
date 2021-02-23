package com.tschasmine.dependency.analysis.backend

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import java.io.File
import java.util.Optional
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(AstAnalyzer::class.java)

class AstAnalyzer(private val file: File) {

    private val compilationUnit: CompilationUnit = StaticJavaParser.parse(file)

    /**
     * Gets fully qualified class names from all declared classes of [file].
     *
     * @return A list of fully qualified class names.
     */
    fun getClasses(): List<String> = compilationUnit.types.map {
            it.fullyQualifiedName.unwrap()
                    ?: "".also { _ -> logger.warn("Could not get fully qualified name for ${it.nameAsString}.") }
        }.filter { it.isNotEmpty() }

    /**
     * Gets all imports.
     *
     * @return A list of [Import]s of the imported classes.
     */
    fun getImports(): List<Import> = compilationUnit.imports
            .map {
                when {
                    it.isAsterisk && it.isStatic -> Import(it.nameAsString, ImportType.STATIC_ASTERISK)
                    it.isAsterisk -> Import(it.nameAsString, ImportType.ASTERISK)
                    it.isStatic -> Import(it.nameAsString, ImportType.STATIC)
                    else -> Import(it.nameAsString, ImportType.NORMAL)
                }
            }

    private fun <T> Optional<T>.unwrap(): T? = orElse(null)

}

data class Import(val name: String, val type: ImportType)

enum class ImportType {
    NORMAL,
    STATIC,
    ASTERISK,
    STATIC_ASTERISK
}
