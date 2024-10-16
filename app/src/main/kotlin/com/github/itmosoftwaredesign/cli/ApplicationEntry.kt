package com.github.itmosoftwaredesign.cli

import com.github.itmosoftwaredesign.cli.command.CommandRegistry
import com.github.itmosoftwaredesign.cli.command.impl.*
import com.github.itmosoftwaredesign.cli.command.parser.CommandParser
import sun.misc.Signal
import kotlin.system.exitProcess

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
        Signal.handle(
            Signal("INT")
        )
        { _ ->  }

        val environment = Environment()

        val parser = CommandParser(environment)
        val commandRegistry = CommandRegistry()
        commandRegistry.register("cat", CatCommand())
        commandRegistry.register("pwd", PrintWorkingDirectoryCommand())
        commandRegistry.register("cd", ChangeDirectoryCommand())
        commandRegistry.register("echo", EchoCommand())
        commandRegistry.register("wc", WcCommand())
        commandRegistry.register("exit", ExitCommand())

        val interpreter = Interpreter(environment, parser, commandRegistry, System.`in`)
        val interpreterThread = Thread(interpreter)
        interpreterThread.start()
        interpreterThread.join()
        exitProcess(environment.lastStatusCode)
    }
}
