package com.github.itmosoftwaredesign.cli.command.impl

import com.github.itmosoftwaredesign.cli.Environment
import com.github.itmosoftwaredesign.cli.command.Command
import com.github.itmosoftwaredesign.cli.command.CommandResult
import com.github.itmosoftwaredesign.cli.command.ErrorResult
import com.github.itmosoftwaredesign.cli.command.SuccessResult
import com.github.itmosoftwaredesign.cli.writeLineUTF8
import jakarta.annotation.Nonnull
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 *  `Grep` command definition.
 *
 *  Finds matches in the file according to the regex.
 *
 *  Supported options:
 *      - '-A {arg}': prints the last matched line plus {arg} lines after
 *      - '-w': searches for the exact matches of the word
 *      - '-i': makes case-insensitive search
 *
 * @author gkashin
 * @since 0.0.1
 */
class GrepCommand : Command {

    override fun execute(
        @Nonnull environment: Environment,
        @Nonnull inputStream: InputStream,
        @Nonnull outputStream: OutputStream,
        @Nonnull errorStream: OutputStream,
        @Nonnull arguments: List<String>
    ): CommandResult {
        if (arguments.size < 2) {
            errorStream.writeLineUTF8("usage: grep [-abcdDEFGHhIiJLlMmnOopqRSsUVvwXxZz] [-A num] [-i string] [-w string] [file ...]")
            return ErrorResult(1)
        }

        val options = Options()
        options.addOption("i", "i", false, "Case-insensitive search.")
        options.addOption("w", "w", false, "Whole word only search.")
        options.addOption("A", "A", true, "Append {ARG} lines to the output.")

        val parser = DefaultParser()
        val cmd = parser.parse(options, arguments.toTypedArray())

        var pattern = cmd.argList.first()
        val fileName = cmd.argList.last()
        val file = File(fileName)
        val optionsSet = mutableSetOf<RegexOption>()

        if (cmd.hasOption("i")) {
            optionsSet.add(RegexOption.IGNORE_CASE)
        }

        if (cmd.hasOption("w")) {
            pattern = "\\b$pattern\\b"
        }

        var linesAfter = 0
        if (cmd.hasOption("A")) {
            linesAfter = cmd.getOptionValue('A').toInt()
        }

        try {
            file.useLines { lines ->
                val result = mutableListOf<String>()
                val linesList = lines.toList()
                val excludeIndices = mutableSetOf<Int>()
                for (i in linesList.indices) {
                    if (pattern.toRegex(optionsSet).containsMatchIn(linesList[i])) {
                        val end = (i + linesAfter).coerceAtMost(linesList.size - 1)
                        for (j in i..end) {
                            if (!excludeIndices.contains(j)) {
                                result.add(linesList[j])
                            }
                            excludeIndices.add(j)
                        }
                    }
                }

                result.forEach { outputStream.writeLineUTF8(it) }
            }
        } catch (e: IOException) {
            errorStream.writeLineUTF8("File '$fileName' read exception, reason: ${e.message}")
            return ErrorResult(1)
        }

        return SuccessResult()
    }
}
