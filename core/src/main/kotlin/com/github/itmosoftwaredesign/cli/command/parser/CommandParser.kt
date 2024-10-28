package com.github.itmosoftwaredesign.cli.command.parser

import com.github.itmosoftwaredesign.cli.Environment
import java.io.*

/**
 *  Command parser. Parse input string to commands, can use environment for substitution.
 *
 * @author sibmaks
 * @since 0.0.1
 */
class CommandParser(private val environment: Environment) {
    /**
     * Parses the input command line string into tokens, handles I/O redirections, and returns
     * a [ParsedCommand] containing the command tokens and associated input, output, and error streams.
     *
     * @param input the input command line string to parse
     * @return [ParsedCommand] object containing tokenized commands and I/O streams
     */
    fun parse(input: String): ParsedCommand {
        val tokens = tokenize(input)
        val commandTokens = ArrayList<String>()
        var inputStream = System.`in`
        var outputStream: OutputStream = System.out
        var errorStream: OutputStream = System.err
        var inputStreamFile = File("")
        var outputStreamFile = File("")
        var errorStreamFile = File("")

        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            try {
                when (token) {
                    "<" -> {
                        val fileName = tokens[++i]
                        val workingDirectory = environment.workingDirectory
                        val file = workingDirectory.resolve(fileName).toFile()
                        inputStream = FileInputStream(file)
                        inputStreamFile = file
                    }

                    ">" -> {
                        val fileName = tokens[++i]
                        val workingDirectory = environment.workingDirectory
                        val file = workingDirectory.resolve(fileName).toFile()
                        outputStream = FileOutputStream(file)
                        outputStreamFile = file
                    }

                    ">>" -> {
                        val fileName = tokens[++i]
                        val workingDirectory = environment.workingDirectory
                        val file = workingDirectory.resolve(fileName).toFile()
                        outputStream = FileOutputStream(file, true)
                        outputStreamFile = file
                    }

                    "2>" -> {
                        val fileName = tokens[++i]
                        val workingDirectory = environment.workingDirectory
                        val file = workingDirectory.resolve(fileName).toFile()
                        errorStream = FileOutputStream(file)
                        errorStreamFile = file
                    }

                    "2>>" -> {
                        val fileName = tokens[++i]
                        val workingDirectory = environment.workingDirectory
                        val file = workingDirectory.resolve(fileName).toFile()
                        errorStream = FileOutputStream(file, true)
                        errorStreamFile = file
                    }

                    "&>", ">&" -> {
                        val fileName = tokens[++i]
                        val workingDirectory = environment.workingDirectory
                        val file = workingDirectory.resolve(fileName).toFile()
                        errorStream = FileOutputStream(file)
                        outputStream = errorStream
                        outputStreamFile = file
                        errorStreamFile = file
                    }

                    else -> commandTokens.add(token)
                }
            } catch (e: FileNotFoundException) {
                throw IllegalArgumentException("File not found: " + e.message)
            }
            i++
        }

        return ParsedCommand(
            commandTokens,
            inputStream,
            outputStream,
            errorStream,
            outputStreamFile,
            inputStreamFile,
            errorStreamFile
        )
    }

    private fun tokenize(input: String): List<String> {
        val tokens = ArrayList<String>()
        val currentToken = StringBuilder()
        var inSingleQuotes = false
        var inDoubleQuotes = false

        for (element in input) {
            if (element == '\'') {
                inSingleQuotes = !inSingleQuotes
            } else if (element == '"') {
                inDoubleQuotes = !inDoubleQuotes
            } else if (element == ' ' && !inSingleQuotes && !inDoubleQuotes) {
                if (currentToken.isNotEmpty()) {
                    tokens.add(currentToken.toString())
                    currentToken.setLength(0)
                }
            } else {
                currentToken.append(element)
            }
        }

        if (currentToken.isNotEmpty()) {
            tokens.add(currentToken.toString())
        }

        return tokens
    }
}
