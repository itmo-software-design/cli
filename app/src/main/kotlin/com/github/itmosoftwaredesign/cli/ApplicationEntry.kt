package com.github.itmosoftwaredesign.cli

import com.github.itmosoftwaredesign.cli.command.CommandRegistry
import com.github.itmosoftwaredesign.cli.command.impl.*
import com.github.itmosoftwaredesign.cli.command.parser.CommandsParser
import sun.misc.Signal
import java.io.InputStream
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
        val environment = Environment()

        val parser = CommandsParser(environment)
        val commandRegistry = CommandRegistry()
        commandRegistry.register("cat", CatCommand())
        commandRegistry.register("pwd", PrintWorkingDirectoryCommand())
        commandRegistry.register("cd", ChangeDirectoryCommand())
        commandRegistry.register("echo", EchoCommand())
        commandRegistry.register("wc", WcCommand())
        commandRegistry.register("exit", ExitCommand())
        commandRegistry.register("set", SetCommand())

        var askToExit = false
        Signal.handle(
            Signal("INT")
        )
        { _ ->
            if (askToExit) {
                exitProcess(0)
            } else {
                askToExit = true
                println("Ctrl+C pressed. Send another to exit")
            }
        }

        val inputStream: InputStream = ObservableInputStream(System.`in`) {
            askToExit = false
        }
        val interpreter = Interpreter(environment, parser, commandRegistry, inputStream)
        val interpreterThread = Thread(interpreter)
        interpreterThread.start()
        interpreterThread.join()
        exitProcess(environment.lastExitCode)
    }
}
