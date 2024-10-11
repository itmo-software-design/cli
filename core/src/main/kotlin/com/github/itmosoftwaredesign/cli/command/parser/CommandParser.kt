package com.github.itmosoftwaredesign.cli.command.parser

import com.github.itmosoftwaredesign.cli.Environment
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream

/**
 *  Command parser. Parse input string to commands, can use environment for substitution.
 *
 * @author sibmaks
 * @since 0.0.1
 */
class CommandParser(private val environment: Environment) {
    /**
     * Parse command line input on tokens
     *
     * @param input input string tokens
     * @return list of tokens
     */
    fun parse(input: String): ParsedCommand {
        val tokens = tokenize(input)
        val commandTokens = ArrayList<String>()
        var inputStream = System.`in`
        var outputStream: OutputStream = System.out
        var errorStream: OutputStream = System.err

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
                    }

                    ">" -> {
                        val fileName = tokens[++i]
                        val workingDirectory = environment.workingDirectory
                        val file = workingDirectory.resolve(fileName).toFile()
                        outputStream = FileOutputStream(file)
                    }

                    ">>" -> {
                        val fileName = tokens[++i]
                        val workingDirectory = environment.workingDirectory
                        val file = workingDirectory.resolve(fileName).toFile()
                        outputStream = FileOutputStream(file, true)
                    }

                    "2>" -> {
                        val fileName = tokens[++i]
                        val workingDirectory = environment.workingDirectory
                        val file = workingDirectory.resolve(fileName).toFile()
                        errorStream = FileOutputStream(file)
                    }

                    "2>>" -> {
                        val fileName = tokens[++i]
                        val workingDirectory = environment.workingDirectory
                        val file = workingDirectory.resolve(fileName).toFile()
                        errorStream = FileOutputStream(file, true)
                    }

                    "&>", ">&" -> {
                        val fileName = tokens[++i]
                        val workingDirectory = environment.workingDirectory
                        val file = workingDirectory.resolve(fileName).toFile()
                        errorStream = FileOutputStream(file)
                        outputStream = errorStream
                    }

                    else -> commandTokens.add(token)
                }
            } catch (e: FileNotFoundException) {
                throw IllegalArgumentException("File not found: " + e.message)
            }
            i++
        }

        return ParsedCommand(commandTokens, inputStream, outputStream, errorStream)
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
