package com.github.itmosoftwaredesign.cli

import com.github.itmosoftwaredesign.cli.command.CommandInterrupted
import com.github.itmosoftwaredesign.cli.command.CommandRegistry
import com.github.itmosoftwaredesign.cli.command.parser.CommandParser
import com.github.itmosoftwaredesign.cli.command.parser.ParsedCommand
import jakarta.annotation.Nonnull
import java.io.InputStream
import java.util.*

/**
 * The Interpreter class is responsible for running a command-line interface loop,
 * reading commands from the input stream, parsing the input, and executing the respective
 * commands. It can handle both registered commands and external programs, allowing interaction
 * with the environment and file redirection.
 *
 * @property environment The environment that stores variables and the working directory.
 * @property parser The CommandParser used to tokenize and parse the input command line.
 * @property registry The CommandRegistry that holds all the available command implementations.
 * @property inputStream The input stream from which commands are read.
 *
 * @author sibmaks
 * @since 0.0.1
 */
class Interpreter(
    @param:Nonnull private val environment: Environment,
    @param:Nonnull private val parser: CommandParser,
    @param:Nonnull private val registry: CommandRegistry,
    @param:Nonnull private val inputStream: InputStream,
) : Runnable {

    /**
     * Runs the interpreter in a loop, reading commands from the input stream and executing them.
     * The loop continues until an "exit" command is received, the input stream ends, or the thread is interrupted.
     *
     * Each command is parsed using the CommandParser, and if it matches a registered command,
     * it is executed. If the command is not found in the registry, the interpreter attempts
     * to run it as an external program.
     *
     * If I/O redirection is specified (e.g., "<", ">", "2>"), it will be handled appropriately
     * by redirecting streams to files or the standard I/O. The environment's last status code
     * is updated after every command execution.
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
            val command = registry[commandAlias]
            try {
                if (command == null) {
                    System.out.printf(
                        "Command '%s' is not registered. Starting execution of the external program...%n",
                        commandAlias
                    )
                    environment.lastStatusCode = runExternalCommand(parsedCommand, tokens.subList(1, tokens.size))
                    println("External program execution finished with exit code ${environment.lastStatusCode}")
                    continue
                }
                var interrupted = false
                parsedCommand.use {
                    val result = command.execute(
                        environment,
                        parsedCommand.inputStream,
                        parsedCommand.outputStream,
                        parsedCommand.errorStream,
                        tokens.subList(1, tokens.size)
                    )
                    environment.lastStatusCode = result.statusCode
                    if (result is CommandInterrupted) {
                        interrupted = true
                    }
                }
                if (interrupted) {
                    break
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
