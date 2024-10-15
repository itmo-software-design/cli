package com.github.itmosoftwaredesign.cli

import com.github.itmosoftwaredesign.cli.command.CommandRegistry
import com.github.itmosoftwaredesign.cli.command.parser.CommandParser
import com.github.itmosoftwaredesign.cli.command.parser.ParsedCommand
import jakarta.annotation.Nonnull
import java.io.InputStream
import java.util.*

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
                    val exitCode = runExternalCommand(parsedCommand, tokens.subList(1, tokens.size))
                    println("External program execution finished with exit code $exitCode")
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

    fun runExternalCommand(parsedCommand: ParsedCommand, arguments: List<String>): Int {
        val commandAlias = parsedCommand.commandTokens.first()
        val args = arguments.toMutableList()
        args.addFirst(commandAlias)
        val dir =
            if (environment.workingDirectory.toString().isEmpty()) null else environment.workingDirectory.toFile()

        val builder = ProcessBuilder(args)
            .directory(dir)
        if (parsedCommand.outputStreamFile.toString().isNotEmpty()) {
            builder.redirectOutput(parsedCommand.outputStreamFile)
        } else {
            builder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
        }

        if (parsedCommand.inputStreamFile.toString().isNotEmpty()) {
            builder.redirectInput(parsedCommand.inputStreamFile)
        } else {
            builder.redirectInput(ProcessBuilder.Redirect.INHERIT)
        }

        if (parsedCommand.errorStreamFile.toString().isNotEmpty()) {
            builder.redirectError(parsedCommand.errorStreamFile)
        } else {
            builder.redirectError(ProcessBuilder.Redirect.INHERIT)
        }

        val variableNames = environment.getVariableNames()
        val processEnvironment = builder.environment()
        for (variableName in variableNames) {
            processEnvironment[variableName] = environment.getVariable(variableName)
        }
        val process = builder.start()
        return process.waitFor()
    }
}
