package com.github.itmosoftwaredesign.cli

import com.github.itmosoftwaredesign.cli.command.CommandRegistry
import com.github.itmosoftwaredesign.cli.command.impl.CatCommand
import com.github.itmosoftwaredesign.cli.command.impl.ChangeDirectoryCommand
import com.github.itmosoftwaredesign.cli.command.impl.PrintWorkingDirectoryCommand
import com.github.itmosoftwaredesign.cli.command.parser.CommandParser

/**
 *  Application entry point
 *
 * @author sibmaks
 * @since 0.0.1
 */
object ApplicationEntry {

    /**
     * Application entry point
     */
    @Throws(InterruptedException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val environment = Environment()

        val parser = CommandParser(environment)
        val commandRegistry = CommandRegistry()
        commandRegistry.register("cat", CatCommand())
        commandRegistry.register("pwd", PrintWorkingDirectoryCommand())
        commandRegistry.register("cd", ChangeDirectoryCommand())

        val interpreter = Interpreter(environment, parser, commandRegistry, System.`in`)
        val interpreterThread = Thread(interpreter)
        interpreterThread.start()
        interpreterThread.join()
    }
}