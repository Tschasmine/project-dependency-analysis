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
    private val thirdPartyOption = Option.builder("t").longOpt("with-third-party")
            .desc("Whether to show third-party dependencies.").build()
    private val projectOption = Option.builder("p").longOpt("project").hasArg()
            .desc("Only output this sub-project's dependencies.").build()
    private val detailsOption = Option.builder("i").longOpt("imports")
            .desc("Show import level dependencies.").build()
    private val outputFileOption = Option.builder("f").longOpt("file").hasArg()
            .desc("Output directory path (defaults to ./build/)").build()
    private val largeOption = Option.builder("l").longOpt("large")
            .desc("Speed up large dot file rendering").build()

    private val options = Options()
            .addOption(helpOption)
            .addOption(verboseOption)
            .addOption(directoryOption)
            .addOption(thirdPartyOption)
            .addOption(projectOption)
            .addOption(detailsOption)
            .addOption(outputFileOption)
            .addOption(largeOption)

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

            val thirdParty = commandLine.hasOption(thirdPartyOption)
            val project = if (commandLine.hasOption(projectOption)) {
                commandLine.getOptionValue(projectOption)
            } else {
                ""
            }

            val outputDir = if(commandLine.hasOption(outputFileOption)) {
                commandLine.getOptionValue(outputFileOption)
            } else {
                ""
            }

            if (commandLine.hasOption(detailsOption)) {
                DotFileBuilder(outputDir).apply {
                    render(detailedBuild(File(commandLine.getOptionValue(directoryOption)), project), commandLine.hasOption(largeOption))
                }
            } else {
                DotFileBuilder(outputDir).apply {
                    render(build(File(commandLine.getOptionValue(directoryOption)), project, thirdParty))
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
