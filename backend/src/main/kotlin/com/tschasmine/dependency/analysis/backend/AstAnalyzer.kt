package com.tschasmine.dependency.analysis.backend

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import java.io.File
import java.util.Optional

class AstAnalyzer(private val file: File) {

    private val compilationUnit: CompilationUnit = StaticJavaParser.parse(file)

    /**
     * Gets fully qualified class names from all declared classes of [file].
     *
     * @return A list of fully qualified class names.
     */
    fun getClasses(): List<String> = compilationUnit.types.map {
            it.fullyQualifiedName.unwrap() ?: ""
        }.filter { it.isNotEmpty() }

    /**
     * Gets all non-static and non-asterisk imports.
     *
     * @return A list of names of the imported classes.
     */
    fun getImports(): List<String> = compilationUnit.imports
            .filter { !it.isAsterisk && !it.isStatic }
            .map { it.nameAsString }

    /**
     * Gets all asterisk imports.
     *
     * @return A list of names of the imported classes.
     */
    fun getAsteriskImports(): List<String> = compilationUnit.imports
            .filter { it.isAsterisk && !it.isStatic }
            .map { it.nameAsString }

    /**
     * Gets all static imports.
     *
     * @return A list of names of the imported classes.
     */
    fun getStaticImports(): List<String> = compilationUnit.imports
            .filter { it.isStatic && !it.isAsterisk }
            .map { it.nameAsString }

    /**
     * Gets all static asterisk imports.
     *
     * @return A list of names of the imported classes.
     */
    fun getStaticAsteriskImports(): List<String> = compilationUnit.imports
            .filter { it.isStatic && it.isAsterisk }
            .map { it.nameAsString }

    private fun <T> Optional<T>.unwrap(): T? = orElse(null)

}
