package com.github.itmosoftwaredesign.cli

import com.github.itmosoftwaredesign.cli.command.CommandRegistry
import com.github.itmosoftwaredesign.cli.command.parser.CommandParser
import com.github.itmosoftwaredesign.cli.command.parser.ParsedCommand
import jakarta.annotation.Nonnull
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit

class Interpreter(
    @param:Nonnull private val environment: Environment,
    @param:Nonnull private val parser: CommandParser,
    @param:Nonnull private val registry: CommandRegistry,
    @param:Nonnull private val inputStream: InputStream,
) : Runnable {

    /**
     * Runs the interpreter. Read commands from input stream and execute in one by one.
     * Until exit command will not be received or end of input stream will not be reached.
     */
    override fun run() {
        val scanner = Scanner(inputStream)
        while (!Thread.interrupted()) {
            print("> ")
            if (!scanner.hasNextLine()) {
                break
            }
            val input = scanner.nextLine()
            val parsedCommand = parser.parse(input)
            val tokens = parsedCommand.commandTokens
            if (tokens.isEmpty()) {
                continue
            }
            val commandAlias = tokens.first()
            if ("exit" == commandAlias) {
                break
            }
            val command = registry[commandAlias]
            try {
                if (command == null) {
                    System.out.printf(
                        "Command '%s' is not registered. Starting execution of the external program...%n",
                        commandAlias
                    )
                    runExternalCommand(parsedCommand, tokens.subList(1, tokens.size))
                    continue
                }
                parsedCommand.use {
                    command.execute(
                        environment,
                        parsedCommand.inputStream,
                        parsedCommand.outputStream,
                        parsedCommand.errorStream,
                        tokens.subList(1, tokens.size)
                    )
                }
            } catch (e: Exception) {
                System.err.printf("Command '%s' execution exception%n", commandAlias)
                e.printStackTrace()
            }
        }
    }

    fun runExternalCommand(parsedCommand: ParsedCommand, arguments: List<String>) {
        val commandAlias = parsedCommand.commandTokens.first()
        val args = arguments.toMutableList()
        args.addFirst(commandAlias)
        val dir =
            if (environment.workingDirectory.toString().isEmpty()) null else environment.workingDirectory.toFile()

        val process = ProcessBuilder(args)
        process.directory(dir)
        if (parsedCommand.outputStreamFile.toString().isNotEmpty()) {
            process.redirectOutput(parsedCommand.outputStreamFile)
        } else {
            process.redirectOutput(ProcessBuilder.Redirect.INHERIT)
        }

        if (parsedCommand.inputStreamFile.toString().isNotEmpty()) {
            process.redirectInput(parsedCommand.inputStreamFile)
        } else {
            process.redirectInput(ProcessBuilder.Redirect.INHERIT)
        }

        if (parsedCommand.errorStreamFile.toString().isNotEmpty()) {
            process.redirectError(parsedCommand.errorStreamFile)
        } else {
            process.redirectError(ProcessBuilder.Redirect.INHERIT)
        }

        process.start().waitFor(5, TimeUnit.SECONDS)
    }
}
