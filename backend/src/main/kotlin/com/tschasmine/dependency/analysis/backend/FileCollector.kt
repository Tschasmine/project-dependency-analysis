package com.tschasmine.dependency.analysis.backend

import java.io.File
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class FileCollector(private val rootDir: File) {

    private val logger: Logger = LoggerFactory.getLogger(FileCollector::class.java)

    /**
     * Recursively collects all files with [extension] from a specific [dir].
     *
     * @param extension The file extension of the files to collect.
     * @param withTests Whether to consider the test source sets identified by 'src/test/' (defaults to true).
     * @param dir The root directory (defaults to [rootDir]).
     *
     * @return A list of names of all files with [extension] within [dir].
     */
    fun collectFiles(extension: String, withTests: Boolean = true, dir: File = rootDir): List<File> {
        return (dir.listFiles { file ->
            file.isDirectory
        }.normalize()).flatMap {
            collectFiles(extension, withTests, it)
        }.plus(dir.listFiles { file ->
            val relativPath = file.relativeTo(rootDir).path
            if (file.isFile) {
                logger.debug("Path: '$relativPath' relativ to '$rootDir'")
            }
            file.isFile && file.extension == extension
                    && (withTests || !relativPath.contains("src/test"))
        }.normalize())
    }

    private fun Array<File>?.normalize(): List<File> =
            this?.toList() ?: emptyList()
}
