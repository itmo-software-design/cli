package com.github.itmosoftwaredesign.cli

import com.github.itmosoftwaredesign.cli.command.CommandRegistry
import com.github.itmosoftwaredesign.cli.command.parser.CommandParser
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
            if (command == null) {
                System.err.println("Unknown command: $commandAlias")
                continue
            }
            try {
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
}
