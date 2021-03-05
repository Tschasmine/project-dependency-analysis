package com.tschasmine.dependency.analysis.app

import java.io.File
import kotlin.system.exitProcess
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("com.tschasmine.dependency.analysis.app.App")

fun main(args: Array<String>) {
    Arguments.parse(args)
}

object Arguments {
    private val helpOption = Option.builder("h").longOpt("help").build()
    private val verboseOption = Option.builder("v").longOpt("verbose")
            .desc("Sets log level to debug.").build()
    private val directoryOption = Option.builder("d").hasArg().longOpt("dir")
            .desc("The project's root directory.").build()
    private val classOption = Option.builder("c").longOpt("class").hasArg()
            .desc("Only output this class' dependencies.").build()
    private val detailsOption = Option.builder("i").longOpt("imports")
            .desc("Show import level dependencies.").build()
    private val outputFileOption = Option.builder("f").longOpt("file").hasArg()
            .desc("Output directory path (defaults to ./build/)").build()
    private val largeOption = Option.builder("l").longOpt("large")
            .desc("Speed up large dot file rendering (does not support clusters).").build()
    private val transitiveOption = Option.builder("t").longOpt("filter-transitive")
            .desc("Whether to filter transitive dependencies.").build()

    private val options = Options()
            .addOption(helpOption)
            .addOption(verboseOption)
            .addOption(directoryOption)
            .addOption(classOption)
            .addOption(detailsOption)
            .addOption(outputFileOption)
            .addOption(largeOption)
            .addOption(transitiveOption)

    fun parse(args: Array<String>) {
        try {
            val commandLine = DefaultParser().parse(options, args)

            if (commandLine.hasOption(helpOption)) {
                printHelpAndExit()
            }

            if (!commandLine.hasOption(directoryOption) || commandLine.getOptionValue(directoryOption).isBlank()) {
                logger.error("Missing required option: '$directoryOption'")
                printHelpAndExit(1)
            }

            val outputDir = if(commandLine.hasOption(outputFileOption)) {
                commandLine.getOptionValue(outputFileOption)
            } else {
                ""
            }

            val transitive = commandLine.hasOption(transitiveOption)

            when {
                commandLine.hasOption(classOption) -> {
                    val singleClass = commandLine.getOptionValue(classOption)
                    DotFileBuilder(outputDir).apply {
                        render(classView(File(commandLine.getOptionValue(directoryOption)), singleClass, transitive), commandLine.hasOption(largeOption))
                    }
                }
                commandLine.hasOption(detailsOption) -> {
                    DotFileBuilder(outputDir).apply {
                        render(detailedBuild(File(commandLine.getOptionValue(directoryOption)), transitive), commandLine.hasOption(largeOption))
                    }
                }
                else -> {
                    DotFileBuilder(outputDir).apply {
                        render(build(File(commandLine.getOptionValue(directoryOption)), transitive))
                    }
                }
            }

        } catch (e: ParseException) {
            logger.error(e.message)
            logger.debug(e.message, e)
            printHelpAndExit(1)
        }

    }

    private fun printHelpAndExit(status: Int = 0) {
        HelpFormatter().printHelp("./analyzer",
                "Analyzes and visualizes a projects sub-project dependencies.",
                options,
                ""
        )
        exitProcess(status)
    }
}

private fun CommandLine.getOptionValue(option: Option) = if (option.hasLongOpt()) {
    this.getOptionValue(option.longOpt)
} else {
    this.getOptionValue(option.opt)
}

private fun CommandLine.hasOption(option: Option) =
        (option.hasLongOpt() && this.hasOption(option.longOpt))
                || this.hasOption(option.opt)
