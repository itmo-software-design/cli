package com.github.itmosoftwaredesign.cli.command.impl

import com.github.itmosoftwaredesign.cli.Environment
import com.github.itmosoftwaredesign.cli.command.Command
import com.github.itmosoftwaredesign.cli.writeLineUTF8
import com.github.itmosoftwaredesign.cli.writeUTF8
import jakarta.annotation.Nonnull
import java.io.*

/**
 *  `Wc` command definition
 *
 * @author gkashin
 * @since 0.0.1
 */
class WcCommand : Command {
    override fun execute(
        @Nonnull environment: Environment,
        @Nonnull inputStream: InputStream,
        @Nonnull outputStream: OutputStream,
        @Nonnull errorStream: OutputStream,
        @Nonnull arguments: List<String>
    ) {
        if (arguments.isEmpty()) {
            val triple = calculate(inputStream.bufferedReader())
            val result = concatenate(triple)
            outputStream.writeLineUTF8(result)
            return
        }
        var totalLinesCount = 0
        var totalWordsCount = 0
        var totalBytesCount = 0
        var successfulExecutionCount = 0
        for (argument in arguments) {
            try {
                val triple = calculate(BufferedReader(FileReader(argument)))
                totalLinesCount += triple.first
                totalWordsCount += triple.second
                totalBytesCount += triple.third
                val result = concatenate(triple)
                outputStream.writeLineUTF8("$result $argument")
                successfulExecutionCount += 1
            } catch (e: IOException) {
                errorStream.writeLineUTF8("Reading input file $argument exception, reason: ${e.message}")
            }
        }
        if (successfulExecutionCount > 1) {
            val totalResult = concatenate(Triple(totalLinesCount, totalWordsCount, totalBytesCount))
            outputStream.writeUTF8("$totalResult total")
        }
    }

    private fun calculate(reader: BufferedReader): Triple<Int, Int, Int> {
        var linesCount = 0
        var wordsCount = 0
        var bytesCount = 0

        var line: String
        while (reader.readLine().also { line = it } != null) {
            linesCount += 1
            wordsCount += line.split(" ", "\t").filter { it.isNotEmpty() }.size
            bytesCount += line.length + 1
        }

        return Triple(linesCount, wordsCount, bytesCount)
    }

    private fun concatenate(triple: Triple<Int, Int, Int>): String {
        return "    ${triple.first}    ${triple.second}    ${triple.third}"
    }
}
