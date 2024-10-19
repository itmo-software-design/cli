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

        var i = 0

        while (i < input.length) {
            val element = input[i]
            when {
                element == '\'' -> {
                    inSingleQuotes = !inSingleQuotes
                    if (!inSingleQuotes) {
                        val parsedToken = currentToken.toString().replace("\\n", "\n")
                        currentToken.setLength(0)
                        currentToken.append(parsedToken)
                    }
                }

                element == '"' -> {
                    inDoubleQuotes = !inDoubleQuotes
                }

                element == ' ' && !inSingleQuotes && !inDoubleQuotes && currentToken.isNotEmpty() -> {
                    tokens.add(currentToken.toString())
                    currentToken.setLength(0)
                }

                element == '$' && !inSingleQuotes -> {
                    val variableResult = substituteVariableAtIndex(input, i)
                    currentToken.append(variableResult.first)
                    i += variableResult.second - 1
                }

                element == '=' && currentToken.isNotEmpty() && !(inSingleQuotes || inDoubleQuotes || tokens.isNotEmpty()) -> {
                    tokens.add("set")
                    tokens.add(currentToken.toString())
                    currentToken.setLength(0)
                }

                else -> {
                    if (!inSingleQuotes || element != '\\' || i + 1 >= input.length) {
                        currentToken.append(element)
                    } else {
                        when (input[i + 1]) {
                            'n' -> {
                                currentToken.append('\n')
                                i++
                            }

                            else -> currentToken.append(element)
                        }
                    }
                }
            }
            i++
        }

        if (currentToken.isNotEmpty()) {
            tokens.add(currentToken.toString())
        }

        return tokens
    }

    private fun substituteVariableAtIndex(input: String, index: Int): Pair<String, Int> {
        val variableBuilder = StringBuilder()
        var i = index + 1

        if (i < input.length && input[i] == '{') {
            i++
            while (i < input.length && input[i] != '}') {
                variableBuilder.append(input[i])
                i++
            }
            if (i < input.length && input[i] == '}') {
                i++
            }
            return buildResult(variableBuilder, i, index)
        }
        if (i < input.length && (input[i] == '$' || input[i] == '?')) {
            variableBuilder.append(input[i])
            i++
            return buildResult(variableBuilder, i, index)
        }
        while (i < input.length && (input[i].isLetterOrDigit() || input[i] == '_')) {
            variableBuilder.append(input[i])
            i++
        }
        return buildResult(variableBuilder, i, index)
    }

    private fun buildResult(
        variableBuilder: StringBuilder,
        i: Int,
        index: Int
    ): Pair<String, Int> {
        val variableName = variableBuilder.toString()
        val variableValue = environment.getVariable(variableName) ?: ""
        return Pair(variableValue, i - index)
    }
}
